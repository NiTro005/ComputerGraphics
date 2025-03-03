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
}
