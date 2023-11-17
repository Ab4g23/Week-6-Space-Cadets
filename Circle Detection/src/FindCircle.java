import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class FindCircle {

    public static BufferedImage toGrayscale(BufferedImage image) {
        for(int y = 0; y < image.getHeight(); y++){
            for(int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x,y);

                int alpha = (pixel>>24)&0xff;
                int red = (pixel & 0xff0000) >> 16; //using bit masking to get the individual amounts of rgb
                int green = (pixel & 0xff00) >> 8;
                int blue = pixel & 0xff;

                int avg = (red+green+blue)/3;

                pixel = (alpha<<24) | (avg<<16) | (avg<<8) | avg;
                image.setRGB(x, y, pixel);
            }
        }

        return image;
    }

    public static void seeImage(BufferedImage image) {
        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setVisible(true);
    }

    public static Integer getGx(BufferedImage image, int x, int y) {
        int gx = 0;
        //gx =  -1 0 1
        //      -2 0 2
        //      -1 0 1

        int currentValue = -1 * image.getRGB(x-1,y-1);
        gx = gx + currentValue;
        currentValue = -2 * image.getRGB(x-1,y);
        gx = gx + currentValue;
        currentValue = -1 * image.getRGB(x-1,y+1);
        gx = gx + currentValue;

        currentValue = image.getRGB(x+1,y-1);
        gx = gx + currentValue;
        currentValue = 2 * image.getRGB(x+1,y);
        gx = gx + currentValue;
        currentValue = image.getRGB(x+1,y+1);
        gx = gx + currentValue;

        return gx;
    }

    public static Integer getGy(BufferedImage image, int x, int y) {
        int gy = 0;
        //gx =  -1 -2 -1
        //       0  0  0
        //       1  2  1

        int currentValue = -1 * image.getRGB(x-1,y-1);
        gy = gy + currentValue;
        currentValue = -2 * image.getRGB(x,y-1);
        gy = gy + currentValue;
        currentValue = -1 * image.getRGB(x+1,y-1);
        gy = gy + currentValue;

        currentValue = image.getRGB(x-1,y+1);
        gy = gy + currentValue;
        currentValue = 2 * image.getRGB(x,y+1);
        gy = gy + currentValue;
        currentValue = image.getRGB(x+1,y+1);
        gy = gy + currentValue;

        return gy;
    }

    public static BufferedImage sobelOperator(BufferedImage image, int threshold) {
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), TYPE_INT_RGB);
        int num = 0;
        for(int y = 1; y < (image.getHeight() - 1); y++){
            for(int x = 1; x < (image.getWidth() - 1); x++) {
                int gx = getGx(image, x, y);
                int gy = getGy(image, x, y);
                int g = (int)Math.sqrt((gx*gx)+(gy*gy));

                if (g < threshold) {
                    g = 0;
                } else {
                    num += 1;
                }

                int pixel = image.getRGB(x, y);
                int alpha = (pixel>>24)&0xff;
                pixel = (alpha<<24) | (0<<16) | (0<<8) | g;

                newImage.setRGB(x, y, pixel);
            }
        }
        System.out.println("num = " + num);

        if (num > 275) {
            newImage = sobelOperator(image, (int)(threshold * 1.001) );
        }
        return newImage;
    }

    public static BufferedImage drawCircle(BufferedImage onlyOneRCircle, int r, int cx, int cy) {
        // r^2 = (x - a)^2 + (y - b)^2 where r= radius, (a,b) = centre
        // x = rcos(theta)  y = rsin(theta)

        for (int i = 0; i < 360; i++) {
            double j = Math.toRadians(i);
            int x = (int) (r * Math.cos(j)) + cx;
            int y = (int) (r * Math.sin(j)) + cy;

            if ((y < onlyOneRCircle.getHeight() && y > 0) && (x < onlyOneRCircle.getWidth() && x > 0)) {
                int pixel = onlyOneRCircle.getRGB(x, y);
                int blue = pixel&0xff;

                if (blue <= 255 - 10) {
                    blue = blue + 10;
                }

                pixel = (0<<24) | (20<<16) | (20<<8) | blue;

                onlyOneRCircle.setRGB(x, y, pixel);
            }
        }
        return onlyOneRCircle;
    }

    public static ArrayList<ArrayList<Integer>> checkCircle(BufferedImage oneRCircle, int r, int num, ArrayList<ArrayList<Integer>> foundCircles) throws InterruptedException {
        for (int y = 1; y < (oneRCircle.getHeight() - 1); y++) {
            for (int x = 1; x < (oneRCircle.getWidth() - 1); x++) {
                int blue = oneRCircle.getRGB(x, y) & 0xff;
                if (blue > 230) {
                    ArrayList<Integer> aCircle = new ArrayList<Integer>();
                    aCircle.add(r);
                    aCircle.add(x);
                    aCircle.add(y);

                    foundCircles.add(aCircle);
                }
            }
        }
        return foundCircles;
    }

    public static ArrayList<ArrayList<Integer>> houghTransform(BufferedImage image) throws InterruptedException {
        int height = image.getHeight();
        int width = image.getWidth();
        ArrayList<ArrayList<Integer>> foundCircles = new ArrayList<ArrayList<Integer>>();

        for (int r = 20; r < Math.max(height, width); r++) {
            BufferedImage circle = new BufferedImage(image.getWidth(), image.getHeight(), TYPE_INT_RGB);
            int num = 0;

            for (int y = 1; y < (height - 1); y++) {
                for (int x = 1; x < (width - 1); x++) {
                    int blue = image.getRGB(x, y) & 0xff;
                    if (blue > 0) {
                        num += 1;
                        circle = drawCircle(circle, r, x, y);
                    }
                }
            }

            foundCircles = checkCircle(circle, r, num, foundCircles);
//            seeImage(circle);
//            TimeUnit.MILLISECONDS.sleep(10);
        }

        return foundCircles;
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        BufferedImage firstImage = ImageIO.read(new File("circle.jpg"));
        BufferedImage image = toGrayscale(firstImage);

        BufferedImage SOimage = sobelOperator(image, 44000); //finds edges - higher the value, sharper the change
        seeImage(SOimage);
        TimeUnit.SECONDS.sleep(2);

        ArrayList<ArrayList<Integer>> foundCircles = houghTransform(SOimage); //huh
        System.out.println(foundCircles);

        for (int i = 0; i < foundCircles.size(); i++) {
            ArrayList<Integer> values = foundCircles.get(i);

            for (int k = 0; k < 360; k++) {
                double j = Math.toRadians(k);
                int x = (int) (values.get(0) * Math.cos(j)) + values.get(1);
                int y = (int) (values.get(0) * Math.sin(j)) + values.get(2);

                if ((y < SOimage.getHeight() && y > 0) && (x < SOimage.getWidth() && x > 0)) {
                    int pixel = (0<<24) | (255<<16) | (150<<8) | 150;
                    SOimage.setRGB(x, y, pixel);
                }

            }
        }
        seeImage(SOimage);
    }
}
