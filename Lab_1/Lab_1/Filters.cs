using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Drawing;
using System.ComponentModel;

namespace Lab_1
{
    abstract class Filters
    {
        protected abstract Color calculateNewPixelColor(Bitmap sourceImage, int x, int y);

        public Bitmap process_image(Bitmap sourceImage, BackgroundWorker backgroundWorker)
        {
            int pixelCount = 0;
            int totalPixels = sourceImage.Width * sourceImage.Height;
            Bitmap newImage = new Bitmap(sourceImage.Width, sourceImage.Height);
            for (int i = 0; i < sourceImage.Width; i++)
            {
                if (backgroundWorker.CancellationPending) return null;
                for (int j = 0; j < sourceImage.Height; j++)
                {
                    newImage.SetPixel(i, j, calculateNewPixelColor(sourceImage, i, j));
                    pixelCount++;
                    if(pixelCount != 0)
                    {
                        int progress = (int)((float)pixelCount / totalPixels * 100);
                        backgroundWorker.ReportProgress(progress);
                    }
                }
            }
            backgroundWorker.ReportProgress(100);
            return newImage;
        }

        public int Clamp(int value, int min, int max)
        {
            if (value < min)
            {
                return min;
            }
            if (value > max)
            {
                return max;
            }
            return value;
        }
        public Color pixel(Bitmap sourceImage, int x, int y)
        {
            return calculateNewPixelColor((Bitmap)sourceImage, x, y);
        }
    }

    internal class InverseFilter : Filters
    {
        protected override Color calculateNewPixelColor(Bitmap sourceImage, int x, int y)
        {
            Color source_color = sourceImage.GetPixel(x, y);
            Color new_color = Color.FromArgb(255 - source_color.R, 255 - source_color.G, 255 - source_color.B);
            return new_color;
        }
    }
    internal class GrayScaleFilter: Filters
    {
        protected override Color calculateNewPixelColor(Bitmap sourceImage, int x, int y)
        {
            Color source_color = sourceImage.GetPixel(x, y);
            float Itensity = 0.299f * source_color.R + 0.587f * source_color.G + 0.114f * source_color.B;
            return Color.FromArgb((int)Itensity, (int)Itensity, (int)Itensity);

        }
    }
    internal class SepiaFilter : Filters
    {
        protected override Color calculateNewPixelColor(Bitmap sourceImage, int x, int y)
        {
            int k = 32;
            Color source_color = sourceImage.GetPixel(x, y);
            float Itensity = 0.299f * source_color.R + 0.587f * source_color.G + 0.114f * source_color.B;
            int R = Clamp((int)Itensity + 2 * k, 0, 255);
            int G = Clamp((int)Itensity + (int)(0.5 * k), 0, 255);
            int B = Clamp((int)Itensity - 1 * k, 0, 255);
            return Color.FromArgb(R, G, B);
        }
    }

    internal class BrightFilter : Filters
    {
        private int bright = 50;

        public BrightFilter(int bright)
        {
            this.bright = bright;
        }
        public BrightFilter() { }

        protected override Color calculateNewPixelColor(Bitmap sourceImage, int x, int y)
        {
            Color source_color = sourceImage.GetPixel(x, y);
            int R = Clamp(source_color.R + bright, 0, 255);
            int G = Clamp(source_color.G + bright, 0, 255);
            int B = Clamp(source_color.B + bright, 0, 255);
            return Color.FromArgb(R, G, B);
        }
    }
    internal class ContrastFilter : Filters
    {
        private double contrastFactor = 0;
        public ContrastFilter(double contrastFactor)
        {
            this.contrastFactor = contrastFactor;
        }
        protected override Color calculateNewPixelColor(Bitmap sourceImage, int x, int y)
        {
            Color source_color = sourceImage.GetPixel(x, y);
            double mean = (source_color.R + source_color.G + source_color.B) / 3.0;
            int R = Clamp((int)(source_color.R + (source_color.R - mean) * contrastFactor), 0, 255);
            int G = Clamp((int)(source_color.G + (source_color.G - mean) * contrastFactor), 0, 255);
            int B = Clamp((int)(source_color.B + (source_color.B - mean) * contrastFactor), 0, 255);
            return Color.FromArgb(R, G, B);
        }
    }

    internal class ShiftFilter: Filters
    {
        protected override Color calculateNewPixelColor(Bitmap sourceImage, int x, int y)
        {
            int shiftAmount = 50;
            if (x < shiftAmount) { 
                return Color.FromArgb(0, 0, 0);
            } else {
                return sourceImage.GetPixel(x - shiftAmount, y);
            }
        }
    } 

    internal class GrayWorldFilter: Filters
    {
        private float avgR, avgG, avgB;

        public GrayWorldFilter(Bitmap sourceImage)
        {
            CalculateAverages(sourceImage);
        }

        private void CalculateAverages(Bitmap sourceImage)
        {
            long sumR = 0, sumG = 0, sumB = 0;
            int pixelCount = sourceImage.Width * sourceImage.Height;

            for (int x = 0; x < sourceImage.Width; x++)
            {
                for (int y = 0; y < sourceImage.Height; y++)
                {
                    Color pixelColor = sourceImage.GetPixel(x, y);
                    sumR += pixelColor.R;
                    sumG += pixelColor.G;
                    sumB += pixelColor.B;
                }
            }

            avgR = sumR / (float)pixelCount;
            avgG = sumG / (float)pixelCount;
            avgB = sumB / (float)pixelCount;
        }
        protected override Color calculateNewPixelColor(Bitmap sourceImage, int x, int y)
        {
            Color originalColor = sourceImage.GetPixel(x, y);

            float gray = (avgR + avgG + avgB) / 3.0f;

            int newR = Clamp((int)(originalColor.R * (gray / avgR)), 0, 255);
            int newG = Clamp((int)(originalColor.G * (gray / avgG)), 0, 255);
            int newB = Clamp((int)(originalColor.B * (gray / avgB)), 0, 255);

            return Color.FromArgb(newR, newG, newB);
        }
    }

    internal class AutolevelFilter : Filters
    {
        private int minR, maxR;
        private int minG, maxG;
        private int minB, maxB;
        public AutolevelFilter(Bitmap sourceImage)
        {
            minR = 255;
            maxR = 0;
            minG = 255;
            maxG = 0;
            minB = 255;
            maxB = 0;

            for (int i = 0; i < sourceImage.Width; i++)
            {
                for (int j = 0; j < sourceImage.Height; j++)
                {
                    Color pixelColor = sourceImage.GetPixel(i, j);
                    int r = pixelColor.R;
                    int g = pixelColor.G;
                    int b = pixelColor.B;

                    if (r < minR) minR = r;
                    if (r > maxR) maxR = r;
                    if (g < minG) minG = g;
                    if (g > maxG) maxG = g;
                    if (b < minB) minB = b;
                    if (b > maxB) maxB = b;
                }
            }
        }

        protected override Color calculateNewPixelColor(Bitmap sourceImage, int x, int y)
        {
            Color originalColor = sourceImage.GetPixel(x, y);
            int originalR = originalColor.R;
            int originalG = originalColor.G;
            int originalB = originalColor.B;

            int newR = (int)((float)(originalR - minR) / (maxR - minR) * 255);
            int newG = (int)((float)(originalG - minG) / (maxG - minG) * 255);
            int newB = (int)((float)(originalB - minB) / (maxB - minB) * 255);

            newR = Clamp(newR, 0, 255);
            newG = Clamp(newG, 0, 255);
            newB = Clamp(newB, 0, 255);

            return Color.FromArgb(newR, newG, newB);
        }
    }
    internal class MirrorFilter : Filters
    {
        public enum MirrorType
        {
            Horizontal,
            Vertical
        }

        private MirrorType mirrorType;

        public MirrorFilter(MirrorType mirrorType)
        {
            this.mirrorType = mirrorType;
        }

        protected override Color calculateNewPixelColor(Bitmap sourceImage, int x, int y)
        {
            if (mirrorType == MirrorType.Horizontal)
            {
                int mirroredX = sourceImage.Width - x - 1;
                return sourceImage.GetPixel(mirroredX, y);
            }

            if (mirrorType == MirrorType.Vertical)
            {
                int mirroredY = sourceImage.Height - y - 1;
                return sourceImage.GetPixel(x, mirroredY);
            }

            return sourceImage.GetPixel(x, y);
        }
    }

    internal class MedianFilter : Filters
    {
        protected override Color calculateNewPixelColor(Bitmap sourceImage, int x, int y)
        {
            int radius = 1;
            List<int> redValues = new List<int>();
            List<int> greenValues = new List<int>();
            List<int> blueValues = new List<int>();

            for (int l = -radius; l <= radius; l++)
            {
                for (int k = -radius; k <= radius; k++)
                {
                    int Xneib = Clamp(x + k, 0, sourceImage.Width - 1);
                    int Yneib = Clamp(y + l, 0, sourceImage.Height - 1);
                    Color neibColor = sourceImage.GetPixel(Xneib, Yneib);
                    redValues.Add(neibColor.R);
                    greenValues.Add(neibColor.G);
                    blueValues.Add(neibColor.B);
                }
            }

            redValues.Sort();
            greenValues.Sort();
            blueValues.Sort();

            int medianIndex = redValues.Count / 2;
            return Color.FromArgb(redValues[medianIndex], greenValues[medianIndex], blueValues[medianIndex]);
        }
    }

    internal class GlassFilter : Filters
    {
        private Random random = new Random();

        protected override Color calculateNewPixelColor(Bitmap sourceImage, int x, int y)
        {
            int newX = x + (int)((random.NextDouble() - 0.5) * 10);
            int newY = y + (int)((random.NextDouble() - 0.5) * 10);

            newX = Clamp(newX, 0, sourceImage.Width - 1);
            newY = Clamp(newY, 0, sourceImage.Height - 1);

            Color sourceColor = sourceImage.GetPixel(newX, newY);

            return sourceColor;
        }
    }
}
