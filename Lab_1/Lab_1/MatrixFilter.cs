using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Lab_1
{
    internal class MatrixFilter: Filters
    {
        protected float[,] kernel = null;
        protected MatrixFilter() { }
        protected MatrixFilter(float[,] kernel) { this.kernel = kernel; }
        protected override Color calculateNewPixelColor(Bitmap sourceImage, int x, int y)
        {
            int radiusX = kernel.GetLength(0) / 2;
            int radiusY = kernel.GetLength(1) / 2;
            float resultR = 0;
            float resultG = 0;
            float resultB = 0;
            for (int l = -radiusX; l <= radiusX; l++) {
                for (int k = -radiusY; k <= radiusY; k++) {
                    int Xneib = Clamp(x + k, 0, sourceImage.Width - 1);
                    int Yneib = Clamp(y + l, 0, sourceImage.Height - 1);
                    Color neib_color = sourceImage.GetPixel(Xneib, Yneib);
                    resultR += neib_color.R * kernel[l + radiusX, k + radiusY];
                    resultG += neib_color.G * kernel[l + radiusX, k + radiusY];
                    resultB += neib_color.B * kernel[l + radiusX, k + radiusY];
                }
            }
            return Color.FromArgb(Clamp((int)resultR, 0, 255),
                Clamp((int)resultG, 0, 255), Clamp((int)resultB, 0, 255));
        }

    }
    internal class BlurFilter : MatrixFilter
    {
        public BlurFilter()
        {
            int sizeX = 3;
            int sizeY = 3;
            kernel = new float[sizeX, sizeY];
            for (int i = 0; i < sizeX; i++)
            {
                for (int j = 0; j < sizeY; j++)
                {
                    kernel[i, j] = 1.0f / (float)(sizeX * sizeY);
                }
            }
        }
    }
    internal class GaussianFilter : MatrixFilter
    {
        public GaussianFilter() {
            CreateGausianKarnel(3, 2);
        }

        public void CreateGausianKarnel(int radius, int sigma)
        {
            int size = 2 * radius + 1;
            kernel = new float[size, size];
            float norm = 0;
            for (int i = -radius; i <= radius; i++){
                for (int j = -radius; j <= radius; j++)
                {
                    kernel[i + radius, j + radius] = (float)(Math.Exp(-(i * i + j * j) / (2 * sigma * sigma)));
                    norm += kernel[i + radius, j + radius];
                }
            }
            for (int i = 0; i < size; i++)
            {
                for(int j = 0;j < size; j++)
                {
                    kernel[i, j] /= norm;
                }
            }
        }
    }
}
