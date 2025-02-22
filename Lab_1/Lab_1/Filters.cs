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
                    if(pixelCount == 0)
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
}