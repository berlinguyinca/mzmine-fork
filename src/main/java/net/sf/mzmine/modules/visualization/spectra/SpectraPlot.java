/*
 * Copyright 2006-2012 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.modules.visualization.spectra;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;

import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import net.sf.mzmine.data.Scan;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.visualization.spectra.datasets.IsotopesDataSet;
import net.sf.mzmine.modules.visualization.spectra.datasets.PeakListDataSet;
import net.sf.mzmine.modules.visualization.spectra.datasets.ScanDataSet;
import net.sf.mzmine.modules.visualization.spectra.renderers.ContinuousRenderer;
import net.sf.mzmine.modules.visualization.spectra.renderers.PeakRenderer;
import net.sf.mzmine.modules.visualization.spectra.renderers.SpectraItemLabelGenerator;
import net.sf.mzmine.util.GUIUtils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

/**
 * 
 */
public class SpectraPlot extends ChartPanel {

	private JFreeChart chart;
	private XYPlot plot;

	// initially, plotMode is set to null, until we load first scan
	private PlotMode plotMode = null;

	// peak labels color
	private static final Color labelsColor = Color.darkGray;

	// grid color
	private static final Color gridColor = Color.lightGray;

	// title font
	private static final Font titleFont = new Font("SansSerif", Font.BOLD, 12);
	private static final Font subTitleFont = new Font("SansSerif", Font.PLAIN,
			11);
	private TextTitle chartTitle, chartSubTitle;

	// legend
	private static final Font legendFont = new Font("SansSerif", Font.PLAIN, 11);

	private boolean isotopesVisible = true, peaksVisible = true,
			itemLabelsVisible = true, dataPointsVisible = false;

	// We use our own counter, because plot.getDatasetCount() just keeps
	// increasing even when we remove old data sets
	private int numOfDataSets = 0;

	public SpectraPlot(ActionListener masterPlot) {

		super(null, true);

		setBackground(Color.white);
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

		// initialize the chart by default time series chart from factory
		chart = ChartFactory.createXYLineChart("", // title
				"m/z", // x-axis label
				"Intensity", // y-axis label
				null, // data set
				PlotOrientation.VERTICAL, // orientation
				true, // isotopeFlag, // create legend?
				true, // generate tooltips?
				false // generate URLs?
				);
		chart.setBackgroundPaint(Color.white);
		setChart(chart);

		// title
		chartTitle = chart.getTitle();
		chartTitle.setMargin(5, 0, 0, 0);
		chartTitle.setFont(titleFont);

		chartSubTitle = new TextTitle();
		chartSubTitle.setFont(subTitleFont);
		chartSubTitle.setMargin(5, 0, 0, 0);
		chart.addSubtitle(chartSubTitle);

		// legend constructed by ChartFactory
		LegendTitle legend = chart.getLegend();
		legend.setItemFont(legendFont);
		legend.setFrame(BlockBorder.NONE);

		// disable maximum size (we don't want scaling)
		setMaximumDrawWidth(Integer.MAX_VALUE);
		setMaximumDrawHeight(Integer.MAX_VALUE);
		setMinimumDrawHeight(0);

		// set the plot properties
		plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));

		// set rendering order
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

		// set grid properties
		plot.setDomainGridlinePaint(gridColor);
		plot.setRangeGridlinePaint(gridColor);

		// set crosshair (selection) properties
		plot.setDomainCrosshairVisible(false);
		plot.setRangeCrosshairVisible(false);

		NumberFormat mzFormat = MZmineCore.getConfiguration().getMZFormat();
		NumberFormat intensityFormat = MZmineCore.getConfiguration()
				.getIntensityFormat();

		// set the X axis (retention time) properties
		NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
		xAxis.setNumberFormatOverride(mzFormat);
		xAxis.setUpperMargin(0.001);
		xAxis.setLowerMargin(0.001);
		xAxis.setTickLabelInsets(new RectangleInsets(0, 0, 20, 20));

		// set the Y axis (intensity) properties
		NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
		yAxis.setNumberFormatOverride(intensityFormat);

		// set focusable state to receive key events
		setFocusable(true);

		// register key handlers
		GUIUtils.registerKeyHandler(this, KeyStroke.getKeyStroke("LEFT"),
				masterPlot, "PREVIOUS_SCAN");
		GUIUtils.registerKeyHandler(this, KeyStroke.getKeyStroke("RIGHT"),
				masterPlot, "NEXT_SCAN");
		GUIUtils.registerKeyHandler(this, KeyStroke.getKeyStroke('+'), this,
				"ZOOM_IN");
		GUIUtils.registerKeyHandler(this, KeyStroke.getKeyStroke('-'), this,
				"ZOOM_OUT");

		// add items to popup menu
		if (masterPlot instanceof SpectraVisualizerWindow) {
			JPopupMenu popupMenu = getPopupMenu();

			popupMenu.addSeparator();

			GUIUtils.addMenuItem(popupMenu, "Toggle centroid/continuous mode",
					masterPlot, "TOGGLE_PLOT_MODE");
			GUIUtils.addMenuItem(popupMenu,
					"Toggle displaying of data points in continuous mode",
					masterPlot, "SHOW_DATA_POINTS");
			GUIUtils.addMenuItem(popupMenu, "Toggle displaying of peak values",
					masterPlot, "SHOW_ANNOTATIONS");
			GUIUtils.addMenuItem(popupMenu,
					"Toggle displaying of picked peaks", masterPlot,
					"SHOW_PICKED_PEAKS");

			popupMenu.addSeparator();

			GUIUtils.addMenuItem(popupMenu, "Set axes range", masterPlot,
					"SETUP_AXES");

			GUIUtils.addMenuItem(popupMenu, "Set same range to all windows",
					masterPlot, "SET_SAME_RANGE");

			popupMenu.addSeparator();

			GUIUtils.addMenuItem(popupMenu, "Add isotope pattern", masterPlot,
					"ADD_ISOTOPE_PATTERN");
		}

	}

	/**
	 * This will set either centroid or continuous renderer to the first data
	 * set, assuming that dataset with index 0 contains the raw data.
	 */
	public void setPlotMode(PlotMode plotMode) {

		this.plotMode = plotMode;

		XYDataset dataSet = plot.getDataset(0);
		if (!(dataSet instanceof ScanDataSet))
			return;

		XYItemRenderer newRenderer;
		if (plotMode == PlotMode.CENTROID) {
			newRenderer = new PeakRenderer(SpectraVisualizerWindow.scanColor,
					false);
		} else {
			newRenderer = new ContinuousRenderer(
					SpectraVisualizerWindow.scanColor, false);
			((ContinuousRenderer) newRenderer)
					.setBaseShapesVisible(dataPointsVisible);
		}

		// Add label generator for the dataset
		SpectraItemLabelGenerator labelGenerator = new SpectraItemLabelGenerator(
				this);
		newRenderer.setBaseItemLabelGenerator(labelGenerator);
		newRenderer.setBaseItemLabelsVisible(itemLabelsVisible);
		newRenderer.setBaseItemLabelPaint(labelsColor);

		plot.setRenderer(0, newRenderer);

	}

	public PlotMode getPlotMode() {
		return plotMode;
	}

	public XYPlot getXYPlot() {
		return plot;
	}

	void switchItemLabelsVisible() {
		itemLabelsVisible = !itemLabelsVisible;
		for (int i = 0; i < plot.getDatasetCount(); i++) {
			XYItemRenderer renderer = plot.getRenderer(i);
			renderer.setBaseItemLabelsVisible(itemLabelsVisible);
		}
	}

	void switchDataPointsVisible() {
		dataPointsVisible = !dataPointsVisible;
		for (int i = 0; i < plot.getDatasetCount(); i++) {
			XYItemRenderer renderer = plot.getRenderer(i);
			if (!(renderer instanceof ContinuousRenderer))
				continue;
			ContinuousRenderer contRend = (ContinuousRenderer) renderer;
			contRend.setBaseShapesVisible(dataPointsVisible);
		}
	}

	void switchPickedPeaksVisible() {
		peaksVisible = !peaksVisible;
		for (int i = 0; i < plot.getDatasetCount(); i++) {
			XYDataset dataSet = plot.getDataset(i);
			if (!(dataSet instanceof PeakListDataSet))
				continue;
			XYItemRenderer renderer = plot.getRenderer(i);
			renderer.setBaseSeriesVisible(peaksVisible);
		}
	}

	void switchIsotopePeaksVisible() {
		isotopesVisible = !isotopesVisible;
		for (int i = 0; i < plot.getDatasetCount(); i++) {
			XYDataset dataSet = plot.getDataset(i);
			if (!(dataSet instanceof IsotopesDataSet))
				continue;
			XYItemRenderer renderer = plot.getRenderer(i);
			renderer.setBaseSeriesVisible(isotopesVisible);
		}
	}

	public void setTitle(String title, String subTitle) {
		chartTitle.setText(title);
		chartSubTitle.setText(subTitle);
	}

	/**
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent event) {

		// let the parent handle the event (selection etc.)
		super.mouseClicked(event);

		// request focus to receive key events
		requestFocus();
	}

	public synchronized void removeAllDataSets() {
		for (int i = 0; i < plot.getDatasetCount(); i++) {
			plot.setDataset(i, null);
		}
		numOfDataSets = 0;
	}

	public synchronized void addDataSet(XYDataset dataSet, Color color,
			boolean transparency) {

		XYItemRenderer newRenderer;

		if (dataSet instanceof ScanDataSet) {
			ScanDataSet scanDataSet = (ScanDataSet) dataSet;
			Scan scan = scanDataSet.getScan();
			if (scan.isCentroided())
				newRenderer = new PeakRenderer(color, transparency);
			else {
				newRenderer = new ContinuousRenderer(color, transparency);
				((ContinuousRenderer) newRenderer)
						.setBaseShapesVisible(dataPointsVisible);
			}

			// Add label generator for the dataset
			SpectraItemLabelGenerator labelGenerator = new SpectraItemLabelGenerator(
					this);
			newRenderer.setBaseItemLabelGenerator(labelGenerator);
			newRenderer.setBaseItemLabelsVisible(itemLabelsVisible);
			newRenderer.setBaseItemLabelPaint(labelsColor);

		} else {
			newRenderer = new PeakRenderer(color, transparency);
		}

		plot.setDataset(numOfDataSets, dataSet);
		plot.setRenderer(numOfDataSets, newRenderer);
		numOfDataSets++;

	}

	public synchronized void removePeakListDataSets() {
		for (int i = 0; i < plot.getDatasetCount(); i++) {
			XYDataset dataSet = plot.getDataset(i);
			if (dataSet instanceof PeakListDataSet) {
				plot.setDataset(i, null);
			}
		}
	}

	ScanDataSet getMainScanDataSet() {
		for (int i = 0; i < plot.getDatasetCount(); i++) {
			XYDataset dataSet = plot.getDataset(i);
			if (dataSet instanceof ScanDataSet) {
				return (ScanDataSet) dataSet;
			}
		}
		return null;
	}

}