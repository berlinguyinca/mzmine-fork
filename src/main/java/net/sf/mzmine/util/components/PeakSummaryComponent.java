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
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.util.components;

import net.sf.mzmine.data.*;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.manual.ManualPeakPickerModule;
import net.sf.mzmine.modules.visualization.spectra.SpectraVisualizerModule;
import net.sf.mzmine.modules.visualization.threed.ThreeDVisualizerModule;
import net.sf.mzmine.modules.visualization.tic.PlotType;
import net.sf.mzmine.modules.visualization.tic.TICVisualizerModule;
import net.sf.mzmine.modules.visualization.twod.TwoDVisualizerModule;
import net.sf.mzmine.util.Range;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.HashMap;
import java.util.Map;

public class PeakSummaryComponent extends JPanel implements ActionListener {

	private static final DecimalFormat formatter = new DecimalFormat("###.#");

	private static final Font defaultFont = new Font("SansSerif", Font.PLAIN,
			11);
	private static final Font titleFont = new Font("SansSerif", Font.BOLD, 14);
	private static final Font ratioFont = new Font("SansSerif", Font.PLAIN, 18);

	private static final Dimension xicPreferredSize = new Dimension(350, 70);

	private JButton btnChange, btnShow;
	private JComboBox comboShow;
	private JLabel ratio;

	private PeakSummaryTableModel listElementModel;
	private JTable peaksInfoList;

	private PeakListRow row;

	private static String[] visualizers = {"Chromatogram", "Mass spectrum",
			"Peak in 2D", "Peak in 3D", "MS/MS", "Isotope pattern"};

	private Color bg = new Color(255, 250, 205); // default color

	public PeakSummaryComponent(PeakListRow row, boolean headerVisible,
			boolean ratioVisible, boolean graphVisible, boolean tableVisible,
			boolean buttonsVisible, Color backgroundColor) {
		this(row, row.getRawDataFiles(), headerVisible, ratioVisible,
				graphVisible, tableVisible, buttonsVisible, backgroundColor);
	}

	/**
	 * @param index
	 * @param dataSet
	 * @param fold
	 * @param frame
	 */
	public PeakSummaryComponent(PeakListRow row, RawDataFile[] rawDataFiles,
			boolean headerVisible, boolean ratioVisible, boolean graphVisible,
			boolean tableVisible, boolean buttonsVisible, Color backgroundColor) {

		if (backgroundColor != null) {
			bg = backgroundColor;
		}

		setBackground(bg);

		this.row = row;

		// Get info
		ChromatographicPeak[] peaks = new ChromatographicPeak[rawDataFiles.length];
		for (int i = 0; i < peaks.length; i++) {
			peaks[i] = row.getPeak(rawDataFiles[i]);
		}

		PeakIdentity identity = row.getPreferredPeakIdentity();

		// General container
		JPanel pnlAll = new JPanel(new BorderLayout());
		pnlAll.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		pnlAll.setBackground(bg);

		// Header peak identification & ratio
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		JLabel name, info;
		if (identity != null) {
			name = new JLabel(identity.getName(), SwingUtilities.LEFT);
			StringBuffer buf = new StringBuffer();
			Format mzFormat = MZmineCore.getConfiguration().getMZFormat();
			Format timeFormat = MZmineCore.getConfiguration().getRTFormat();
			buf.append("#" + row.getID() + " ");
			buf.append(mzFormat.format(row.getAverageMZ()));
			buf.append(" m/z @");
			buf.append(timeFormat.format(row.getAverageRT()));
			info = new JLabel(buf.toString(), SwingUtilities.LEFT);
			info.setBackground(bg);
			info.setFont(defaultFont);
			headerPanel.add(name, BorderLayout.NORTH);
			headerPanel.add(info, BorderLayout.CENTER);
		} else {
			name = new JLabel(row.toString(), SwingUtilities.LEFT);
			headerPanel.add(name, BorderLayout.CENTER);
		}

		name.setFont(titleFont);
		name.setBackground(bg);
		headerPanel.setBackground(bg);
		headerPanel.setPreferredSize(new Dimension(290, 50));
		headerPanel.setVisible(headerVisible);

		// Ratio between peaks
		JPanel ratioPanel = new JPanel(new BorderLayout());
		ratio = new JLabel("", SwingUtilities.LEFT);
		ratio.setFont(ratioFont);

		ratio.setBackground(bg);
		ratioPanel.add(ratio, BorderLayout.CENTER);

		ratioPanel.setBackground(bg);
		ratioPanel.setVisible(ratioVisible);

		JPanel headerAndRatioPanel = new JPanel(new BorderLayout());
		headerAndRatioPanel.add(headerPanel, BorderLayout.WEST);
		headerAndRatioPanel.add(Box.createVerticalGlue(), BorderLayout.CENTER);
		headerAndRatioPanel.add(ratioPanel, BorderLayout.EAST);
		headerAndRatioPanel.setBackground(bg);
		pnlAll.add(headerAndRatioPanel, BorderLayout.NORTH);
		// <-

		// Plot section
		JPanel plotPanel = new JPanel();
		plotPanel.setLayout(new BoxLayout(plotPanel, BoxLayout.Y_AXIS));
		Border one = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
		Border two = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		plotPanel.setBorder(BorderFactory.createCompoundBorder(one, two));
		plotPanel.setBackground(Color.white);
		// No tooltip
		CombinedXICComponent xic = new CombinedXICComponent(peaks, -1);
		xic.setPreferredSize(xicPreferredSize);
		plotPanel.add(xic);
		plotPanel.setVisible(graphVisible);
		pnlAll.add(plotPanel, BorderLayout.CENTER);
		// <-

		// Table with peak's information
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
		tablePanel.setBackground(bg);

		listElementModel = new PeakSummaryTableModel();
		peaksInfoList = new JTable();
		peaksInfoList.setModel(listElementModel);
		peaksInfoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		peaksInfoList.setDefaultRenderer(Object.class,
				new PeakSummaryTableCellRenderer());

		int colorIndex = 0;
		Color peakColor;

		for (ChromatographicPeak peak : peaks) {
			// set color for current XIC
			if (peak != null) {
				peakColor = CombinedXICComponent.plotColors[colorIndex];
				listElementModel.addElement(peak, peakColor);
			}
			colorIndex = (colorIndex + 1)
					% CombinedXICComponent.plotColors.length;
		}

		JPanel listPanel = new JPanel(new BorderLayout());
		listPanel.add(new JScrollPane(peaksInfoList), BorderLayout.CENTER);
		listPanel.add(peaksInfoList.getTableHeader(), BorderLayout.NORTH);
		listPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

		Dimension d = calculatedTableDimension(peaksInfoList);
		listPanel.setPreferredSize(d);

		tablePanel.add(Box.createVerticalStrut(5));
		tablePanel.add(listPanel, BorderLayout.CENTER);
		tablePanel.setBackground(bg);
		tablePanel.setVisible(tableVisible);

		// Buttons
		comboShow = new JComboBox(visualizers);

		btnShow = new JButton("Show");
		btnShow.setActionCommand("SHOW");
		btnShow.addActionListener(this);

		btnChange = new JButton("Change");
		btnChange.setActionCommand("CHANGE");
		btnChange.addActionListener(this);

		JPanel pnlShow = new JPanel(new BorderLayout());
		pnlShow.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED));

		pnlShow.add(comboShow, BorderLayout.NORTH);
		pnlShow.add(btnShow, BorderLayout.CENTER);
		pnlShow.setBackground(bg);

		JPanel buttonsPanel = new JPanel(new BorderLayout());
		buttonsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		buttonsPanel.add(pnlShow, BorderLayout.NORTH);
		buttonsPanel.add(Box.createVerticalGlue(), BorderLayout.CENTER);
		buttonsPanel.add(btnChange, BorderLayout.SOUTH);
		buttonsPanel.setBackground(bg);
		buttonsPanel.setVisible(buttonsVisible);

		JPanel buttonsAndTablePanel = new JPanel(new BorderLayout());
		buttonsAndTablePanel.add(tablePanel, BorderLayout.CENTER);
		buttonsAndTablePanel.add(buttonsPanel, BorderLayout.EAST);
		buttonsAndTablePanel.setBackground(bg);

		pnlAll.add(buttonsAndTablePanel, BorderLayout.SOUTH);
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		add(pnlAll, BorderLayout.CENTER);

	}

	public void setRatio(double area1, double area2) {
		String text;
		Color ratioColor;
		if (area1 > area2) {
			text = formatter.format(area1 / area2) + "x";
			ratioColor = CombinedXICComponent.plotColors[0];
		} else {
			text = formatter.format(area2 / area1) + "x";
			ratioColor = CombinedXICComponent.plotColors[1];
		}
		ratio.setForeground(ratioColor);
		ratio.setText(text);
	}

	/**
	 * @param peaksInfoList
	 * @return
	 */
	private Dimension calculatedTableDimension(JTable peaksInfoList) {

		int numRows = peaksInfoList.getRowCount();
		int numCols = peaksInfoList.getColumnCount();
		int maxWidth = 0, compWidth, totalWidth = 0, totalHeight = 0;
		TableCellRenderer renderer = peaksInfoList
				.getDefaultRenderer(Object.class);
		TableCellRenderer headerRenderer = peaksInfoList.getTableHeader()
				.getDefaultRenderer();
		TableModel model = peaksInfoList.getModel();
		Component comp;
		TableColumn column;

		for (int c = 0; c < numCols; c++) {
			for (int r = 0; r < numRows; r++) {

				if (r == 0) {
					comp = headerRenderer.getTableCellRendererComponent(
							peaksInfoList, model.getColumnName(c), false,
							false, r, c);
					compWidth = comp.getPreferredSize().width + 10;
					maxWidth = Math.max(maxWidth, compWidth);

				}

				comp = renderer.getTableCellRendererComponent(peaksInfoList,
						model.getValueAt(r, c), false, false, r, c);

				compWidth = comp.getPreferredSize().width + 10;
				maxWidth = Math.max(maxWidth, compWidth);

				if (c == 0) {
					totalHeight += comp.getPreferredSize().height;
				}

				// Consider max 10 rows
				if (r == 8) {
					break;
				}

			}
			totalWidth += maxWidth;
			column = peaksInfoList.getColumnModel().getColumn(c);
			column.setPreferredWidth(maxWidth);
			maxWidth = 0;
		}

		// add 30x10 px for a scrollbar
		totalWidth += 30;
		totalHeight += 10;

		comp = headerRenderer.getTableCellRendererComponent(peaksInfoList,
				model.getColumnName(0), false, false, 0, 0);
		totalHeight += comp.getPreferredSize().height;

		return new Dimension(totalWidth, totalHeight);

	}

	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if (command.equals("SHOW")) {

			String visualizerType = (String) comboShow.getSelectedItem();
			int[] indexesRow = peaksInfoList.getSelectedRows();
			ChromatographicPeak[] selectedPeaks = new ChromatographicPeak[indexesRow.length];
			RawDataFile[] dataFiles = new RawDataFile[indexesRow.length];
			Range rtRange = null, mzRange = null;
			for (int i = 0; i < indexesRow.length; i++) {
				selectedPeaks[i] = listElementModel.getElementAt(indexesRow[i]);
				dataFiles[i] = selectedPeaks[i].getDataFile();

				if ((rtRange == null) || (mzRange == null)) {
					rtRange = dataFiles[i].getDataRTRange(1);
					mzRange = selectedPeaks[i].getRawDataPointsMZRange();
				} else {
					rtRange.extendRange(dataFiles[i].getDataRTRange(1));
					mzRange.extendRange(selectedPeaks[i]
							.getRawDataPointsMZRange());
				}
			}

			if (dataFiles.length == 0) {
				return;
			}

			if (visualizerType.equals("Chromatogram")) {

				// Label best peak with preferred identity.
				final ChromatographicPeak bestPeak = row.getBestPeak();
				final PeakIdentity peakIdentity = row
						.getPreferredPeakIdentity();
				final Map<ChromatographicPeak, String> labelMap = new HashMap<ChromatographicPeak, String>(
						1);
				if (bestPeak != null && peakIdentity != null) {

					labelMap.put(bestPeak, peakIdentity.getName());
				}

				TICVisualizerModule.showNewTICVisualizerWindow(dataFiles,
						selectedPeaks, labelMap, 1, PlotType.BASEPEAK, rtRange,
						mzRange);
				return;

			} else if (visualizerType.equals("Mass spectrum")) {
				for (int i = 0; i < selectedPeaks.length; i++) {
					SpectraVisualizerModule.showNewSpectrumWindow(dataFiles[i],
							selectedPeaks[i].getRepresentativeScanNumber());
				}
			} else if (visualizerType.equals("Peak in 2D")) {
				for (int i = 0; i < selectedPeaks.length; i++) {
					Range peakRTRange = selectedPeaks[i]
							.getRawDataPointsRTRange();
					Range peakMZRange = selectedPeaks[i]
							.getRawDataPointsMZRange();
					Range localRTRange = new Range(Math.max(0,
							peakRTRange.getMin() - peakRTRange.getSize()),
							peakRTRange.getMax() + peakRTRange.getSize());

					Range localMZRange = new Range(Math.max(0,
							peakMZRange.getMin() - peakMZRange.getSize()),
							peakMZRange.getMax() + peakMZRange.getSize());
					TwoDVisualizerModule.show2DVisualizerSetupDialog(
							dataFiles[i], localMZRange, localRTRange);
				}
			} else if (visualizerType.equals("Peak in 3D")) {
				for (int i = 0; i < selectedPeaks.length; i++) {
					Range peakRTRange = selectedPeaks[i]
							.getRawDataPointsRTRange();
					Range peakMZRange = selectedPeaks[i]
							.getRawDataPointsMZRange();
					Range localRTRange = new Range(Math.max(0,
							peakRTRange.getMin() - peakRTRange.getSize()),
							peakRTRange.getMax() + peakRTRange.getSize());

					Range localMZRange = new Range(Math.max(0,
							peakMZRange.getMin() - peakMZRange.getSize()),
							peakMZRange.getMax() + peakMZRange.getSize());
					ThreeDVisualizerModule.setupNew3DVisualizer(dataFiles[i],
							localMZRange, localRTRange);
				}
			} else if (visualizerType.equals("MS/MS")) {
				for (int i = 0; i < selectedPeaks.length; i++) {
					int scanNumber = selectedPeaks[i]
							.getMostIntenseFragmentScanNumber();
					if (scanNumber > 0) {
						SpectraVisualizerModule.showNewSpectrumWindow(
								dataFiles[i], scanNumber);
					} else {
						MZmineCore.getDesktop().displayMessage(
								"There is no fragment for the mass "
										+ MZmineCore
												.getConfiguration()
												.getMZFormat()
												.format(selectedPeaks[i]
														.getMZ())
										+ "m/z in the current raw data.");
						return;
					}
				}

			} else if (visualizerType.equals("Isotope pattern")) {
				for (int i = 0; i < selectedPeaks.length; i++) {
					IsotopePattern ip = selectedPeaks[i].getIsotopePattern();
					if (ip == null) {
						return;
					}
					SpectraVisualizerModule
							.showNewSpectrumWindow(
									dataFiles[i],
									selectedPeaks[i]
											.getMostIntenseFragmentScanNumber(),
									ip);

				}
			}
			return;
		}

		if (command.equals("CHANGE")) {
			int indexRow = peaksInfoList.getSelectedRow();
			if (indexRow == -1) {
				return;
			}
			ChromatographicPeak selectedPeak = listElementModel
					.getElementAt(indexRow);
			ManualPeakPickerModule.runManualDetection(
					selectedPeak.getDataFile(), row);

			return;
		}

	}

}
