/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.lib.swing;

import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.swing.*;
import net.rptools.lib.image.ImageUtil;

/**
 * An icon that uses the alpha channel of an image to render a single color icon
 * in the target component's foregound color. This allows to use a SVG to PNG
 * sampled image to be used in different look and feels.
 */
public class AlphaForegroundIcon implements Icon {

      private WritableRaster alpha;
      private int w,h;
      private Map<Color, BufferedImage> color2image = new HashMap<>();

      public AlphaForegroundIcon(String path) {

          try {
           alpha = ImageUtil.getCompatibleImage(path).getAlphaRaster();
          } catch (IOException e) {
              throw new IllegalArgumentException("Can't load "+path);
          }
           if (alpha==null) {
              throw new IllegalArgumentException("No alpha in "+path);
           }
           w = alpha.getWidth();
           h = alpha.getHeight();
        }

        private boolean isSelected(Component c) {
          return c instanceof AbstractButton && ((AbstractButton)c).isSelected();
        }

        private BufferedImage getImage(Color color) {
            BufferedImage coloredImage = color2image.get(color);
            if (coloredImage==null) {
                coloredImage = new BufferedImage(new ColorAlphaRasterColorModel(color), alpha, false, new Hashtable());
                color2image.put(color, coloredImage);
            }
            return coloredImage;
        }

      @Override
      public synchronized void paintIcon(Component c, Graphics g, int x, int y) {

          Color color = c.getForeground();
          BufferedImage coloredImage = getImage(color);
          Graphics2D g2d = (Graphics2D)g;

          if (!c.isEnabled()) {
              AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
              g2d.setComposite(composite);
          } else if (isSelected(c)) {
              g2d.setColor(color);
              g2d.setStroke(new BasicStroke(3));
              g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
              g2d.fillOval(x,y,w,h);
              coloredImage = getImage(c.getBackground());
          }

          g.drawImage(coloredImage, x, y, null);

      }

      @Override
      public int getIconWidth() {
          return w;
      }

      @Override
      public int getIconHeight() {
          return h;
      }

    /**
     * Color model that uses a single color for RGB and a given raster for alpha
     */
  private static class ColorAlphaRasterColorModel extends ColorModel {

      private int r,g,b;

      ColorAlphaRasterColorModel(Color color) {
          super(32);
        r  = color.getRed();
        g = color.getGreen();
        b = color.getBlue();
      }

      @Override
      public boolean isCompatibleRaster(Raster raster) {
          return true;
      }

      @Override
      public int getRed(int pixel) {
          return r;
      }

      @Override
      public int getGreen(int pixel) {
          return g;
      }

      @Override
      public int getBlue(int pixel) {
          return b;
      }

      @Override
      public int getAlpha(int pixel) {
          return 0xff&(pixel>>24);
      }
  }



//    DataBuffer dataBuffer = backing.getRaster().getDataBuffer();
//    ColorPatcher patcher = DATABUFFERTYPE_TO_PATCHERS.get(dataBuffer.getDataType());
//      if (patcher==null)
//            return new ImageIcon(backing);
//
//    int w = backing.getWidth();
//    int h = backing.getHeight();
//      patcher.patch(dataBuffer, w, h, foregroundColor);
//

//  private static abstract class ColorPatcher {
//      abstract void patch(DataBuffer buffer, int w, int h, Color color);
//  }
//
//  private static class IntColorPatcher extends ColorPatcher {
//      @Override
//      void patch(DataBuffer buffer, int w, int h, Color color) {
//
//          int[] data = ((DataBufferInt)buffer).getData();
//
//          int r = color.getRed();
//          int g = color.getGreen();
//          int b = color.getBlue();
//
//          for (int i=0;i<w*h;i++) {
//              int a = 0xff&(data[i]>>24)    ;
//              data[i] = (a<<24) | (0xff&(r*a/256))<<16 | (0xff&(g*a/256))<<8 | (0xff&(b*a/256));
//          }
//      }
//  }

}

