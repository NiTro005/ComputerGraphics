using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Drawing;

namespace Lab_1
{
    abstract class Filters
    {
        protected abstract Color calculateNewPixelColor(Bitmap sourceImage, int x, int y);

        public Bitmap process_image(Bitmap sourceImage)
        {
            Bitmap newImage = new Bitmap(sourceImage.Width, sourceImage.Height);
            for (int i = 0; i < sourceImage.Width; i++)
            {
                for (int j = 0; j < sourceImage.Height; j++)
                {
                    newImage.SetPixel(i, j, calculateNewPixelColor(sourceImage, i, j));
                }
            }
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