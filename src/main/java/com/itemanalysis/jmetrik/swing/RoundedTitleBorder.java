/*
 * Copyright 2008 Sun Microsystems, Inc.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.itemanalysis.jmetrik.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

/**
 *
 * @author Administrator
 */
public class RoundedTitleBorder extends RoundedBorder {
    private final String title;
    private final Color[] titleGradientColors;
    protected float hsb[];

    public RoundedTitleBorder(String title) {
        super(10);
        Color titleColor = UIManager.getColor("nimbusBase");
        hsb = Color.RGBtoHSB(titleColor.getRed(), titleColor.getGreen(), titleColor.getBlue(), null);
        this.title = title;
        this.titleGradientColors = new Color[2];
        this.titleGradientColors[0] = Color.getHSBColor(hsb[0] - .013f, .15f, .85f);
        this.titleGradientColors[1] = Color.getHSBColor(hsb[0] - .005f, .24f, .80f);
    }

    public Insets getBorderInsets(Component c, Insets insets) {
        Insets borderInsets = super.getBorderInsets(c, insets);
        borderInsets.top = getTitleHeight(c);
        return borderInsets;
    }

    protected int getTitleHeight(Component c) {
        FontMetrics metrics = c.getFontMetrics(c.getFont());
        return (int)(metrics.getHeight() * 1.80);
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        int titleHeight = getTitleHeight(c);

        c.setForeground(Color.getHSBColor(hsb[0], .54f, .40f));

        // create image with vertical gradient and then use alpha composite to
        // render the image with a horizontal fade
        BufferedImage titleImage = SwingSetUtils.createTranslucentImage(width, titleHeight);
        GradientPaint gradient = new GradientPaint(0, 0,
                titleGradientColors[0], 0, titleHeight,
                titleGradientColors[1], false);
        Graphics2D g2 = (Graphics2D)titleImage.getGraphics();
        g2.setPaint(gradient);
        g2.fillRoundRect(x, y, width, height, 10, 10);
        g2.setColor(SwingSetUtils.deriveColorHSB(
                titleGradientColors[1], 0, 0, -.2f));
        g2.drawLine(x + 1, titleHeight - 1, width - 2, titleHeight - 1);
        g2.setColor(SwingSetUtils.deriveColorHSB(
                titleGradientColors[1], 0, -.5f, .5f));
        g2.drawLine(x + 1, titleHeight, width - 2, titleHeight);
        g2.setPaint(new GradientPaint(0, 0, new Color(0.0f, 0.0f, 0.0f, 1.0f),
                width, 0, new Color(0.0f, 0.0f, 0.0f, 0.0f)));
        g2.setComposite(AlphaComposite.DstIn);
        g2.fillRect(x, y, width, titleHeight);
        g2.dispose();

        g.drawImage(titleImage, x, y, c);

        // draw rounded border
        super.paintBorder(c, g, x, y, width, height);

        //set title color and size -- added by Patrick Meyer
        Font labelFont = UIManager.getFont("Label.font");
//        Font f = labelFont.deriveFont(Font.BOLD, labelFont.getSize()+4f);
        Font f = labelFont.deriveFont(Font.PLAIN, labelFont.getSize());
        c.setFont(f);

        // draw title string
        g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(c.getForeground());
        g2.setFont(c.getFont());
        FontMetrics metrics = c.getFontMetrics(c.getFont());
        g2.drawString(title, x + 8,
                y + (titleHeight - metrics.getHeight())/2 + metrics.getAscent());
        g2.dispose();

    }
}
