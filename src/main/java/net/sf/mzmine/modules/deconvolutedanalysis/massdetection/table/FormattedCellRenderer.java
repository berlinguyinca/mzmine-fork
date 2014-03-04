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

package net.sf.mzmine.modules.deconvolutedanalysis.massdetection.table;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.NumberFormat;

/**
 * Simple table cell renderer that renders Numbers using given NumberFormat
 */
class FormattedCellRenderer implements TableCellRenderer {
	public static final Border padding = BorderFactory.createEmptyBorder(0, 5,
			0, 10);
	private Font font;
	private NumberFormat format;
	private Color alternateBackground;

	FormattedCellRenderer(NumberFormat format, Color alternateBackground) {
		this.format = format;
		this.alternateBackground = alternateBackground;
	}

	FormattedCellRenderer(NumberFormat format, Font font,
			Color alternateBackground) {
		this.format = format;
		this.font = font;
		this.alternateBackground = alternateBackground;
	}

	/**
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
	 *      Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		JPanel newPanel = new JPanel();
		newPanel.setLayout(new OverlayLayout(newPanel));

		newPanel.setBackground(isSelected
				? table.getSelectionBackground()
				: (row % 2 == 0) ? table.getBackground() : alternateBackground);

		if (hasFocus) {
			Border border = null;
			if (isSelected)
				border = UIManager
						.getBorder("Table.focusSelectedCellHighlightBorder");
			if (border == null)
				border = UIManager.getBorder("Table.focusCellHighlightBorder");

			/*
			 * The "border.getBorderInsets(newPanel) != null" is a workaround
			 * for OpenJDK 1.6.0 bug, otherwise setBorder() may throw a
			 * NullPointerException
			 */
			if ((border != null) && (border.getBorderInsets(newPanel) != null))
				newPanel.setBorder(border);
		}

		newPanel.setBorder(BorderFactory.createCompoundBorder(
				newPanel.getBorder(), padding));

		if (value != null) {
			String text;

			if (value instanceof Number)
				text = format.format(value);
			else
				text = value.toString();

			JLabel newLabel = new JLabel(text, JLabel.LEFT);

			if (font != null)
				newLabel.setFont(font);
			else if (table.getFont() != null)
				newLabel.setFont(table.getFont());

			newPanel.add(newLabel);
		}
		return newPanel;
	}
}
