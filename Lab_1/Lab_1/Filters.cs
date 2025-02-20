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
        protected abstract Color calculateNewPixelColor(Bitmap sourseImage, int x, int y);
        public Bitmap process_image(Bitmap sourseImage)
        {
            Bitmap newImage = new Bitmap(sourseImage.Width, sourseImage.Height);
            for (int i = 0; i < sourseImage.Width; i++) { 
                for (int j = 0; j < sourseImage.Height; j++)
                {
                    newImage.SetPixel(i, j, calculateNewPixelColor(sourseImage, i, j));
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
