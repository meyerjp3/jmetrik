/*
 * Copyright (c) 2011 Patrick Meyer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.itemanalysis.jmetrik.swing;

import javax.swing.border.Border;
import java.awt.*;

public class ChiselBorder implements Border {
        private Insets insets = new Insets(1, 0, 1, 0);

        public ChiselBorder() {
        }

        public Insets getBorderInsets(Component c) {
            return insets;
        }
        public boolean isBorderOpaque() {
            return true;
        }
         public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Color color = c.getBackground();
            // render highlight at top
            g.setColor(SwingSetUtils.deriveColorHSB(color, 0, 0, .2f));
            g.drawLine(x, y, x + width, y);
            // render shadow on bottom
            g.setColor(SwingSetUtils.deriveColorHSB(color, 0, 0, -.2f));
            g.drawLine(x, y + height - 1, x + width, y + height - 1);
        }
    }
