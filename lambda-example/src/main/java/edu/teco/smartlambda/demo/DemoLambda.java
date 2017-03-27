package edu.teco.smartlambda.demo;

import edu.teco.smartlambda.execution.LambdaFunction;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class DemoLambda {
	
	/**
	 * This method takes an image and converts it to an ASCII art. It was taken from <a href="https://codehackersblog.blogspot
	 * .com/2015/06/image-to-ascii-art-converter-in-java.html">here</a>
	 *
	 * @param image an image
	 *
	 * @return a string containing the ascii art
	 */
	public String convert(final BufferedImage image) {
		final StringBuilder stringBuilder = new StringBuilder((image.getWidth() / 5 + 1) * image.getHeight() / 10 + 1);
		
		for (int y = 0; y < image.getHeight(); y += 10) {
			if (stringBuilder.length() != 0) stringBuilder.append("\n");
			for (int x = 0; x < image.getWidth(); x += 5) {
				final Color pixelColor = new Color(image.getRGB(x, y));
				final double brightness = (double) pixelColor.getRed() * 0.2989 + (double) pixelColor.getBlue() * 0.5870 +
						(double) pixelColor.getGreen() * 0.1140;
				stringBuilder.append(this.returnStrPos(brightness));
			}
		}
		return stringBuilder.toString();
	}
	
	/**
	 * Create a new string and assign to it a string based on the grayscale value.
	 * If the grayscale value is very high, the pixel is very bright and assign characters
	 * such as . and , that do not appear very dark. If the grayscale value is very low, the pixel is very dark,
	 * assign characters such as # and @ which appear very dark.
	 * <p>
	 * This example code was taken from
	 * <a href="https://codehackersblog.blogspot.com/2015/06/image-to-ascii-art-converter-in-java.html">here</a>
	 *
	 * @param brightness grayscale value
	 *
	 * @return a char applying to the given brightness value
	 */
	private char returnStrPos(final double brightness) {
		final char str;
		
		if (brightness >= 230.0) {
			str = ' ';
		} else if (brightness >= 200.0) {
			str = '.';
		} else if (brightness >= 180.0) {
			str = '*';
		} else if (brightness >= 160.0) {
			str = ':';
		} else if (brightness >= 130.0) {
			str = 'o';
		} else if (brightness >= 100.0) {
			str = '&';
		} else if (brightness >= 70.0) {
			str = '8';
		} else if (brightness >= 50.0) {
			str = '#';
		} else {
			str = '@';
		}
		return str; // return the character
	}
	
	@LambdaFunction
	public AsciiImage doSomeRandomFunnyShit(final Parameter parameter) throws IOException {
		return new AsciiImage(this.convert(ImageIO.read(new URL(parameter.getURL()))));
	}
}
