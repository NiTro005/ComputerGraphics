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
        public MatrixFilter(float[,] kernel) { this.kernel = kernel; }
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
                    resultR += neib_color.R * kernel[k + radiusX, l + radiusY];
                    resultG += neib_color.G * kernel[k + radiusX, l + radiusY];
                    resultB += neib_color.B * kernel[k + radiusX, l + radiusY];
                }
            }
            return Color.FromArgb(Clamp((int)resultR, 0, 255),
                Clamp((int)resultG, 0, 255), Clamp((int)resultB, 0, 255));
        }

        public Color ApplyFilter(Bitmap sourceImage, int x, int y)
        {
            return calculateNewPixelColor(sourceImage, x, y);
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

    internal class SobelFilter : MatrixFilter
    {
        private static readonly float[,] kernelX = new float[3, 3]
        {
            { -1, 0, 1 },
            { -2, 0, 2 },
            { -1, 0, 1 }
        };

        private static readonly float[,] kernelY = new float[3, 3]
        {
            { -1, -2, -1 },
            { 0, 0, 0 },
            { 1, 2, 1 }
        };

        private float CalculateGradient(Bitmap sourceImage, int x,  int y, float[,] kernel)
        {
            MatrixFilter matrix = new MatrixFilter(kernel);
            Color resultColor = matrix.ApplyFilter(sourceImage, x, y);
            float intensity = 0.299f * resultColor.R + 0.587f * resultColor.G + 0.114f * resultColor.B;
            return intensity;
        }
        protected override Color calculateNewPixelColor(Bitmap sourceImage, int x, int y)
        {
            float gradientX = CalculateGradient(sourceImage, x, y, kernelX);
            float gradientY = CalculateGradient(sourceImage, x, y, kernelY);
            int intensity = (int)Math.Sqrt(gradientX * gradientX + gradientY * gradientY);
            intensity = Clamp(intensity, 0, 255);

            return Color.FromArgb(intensity, intensity, intensity);
        }
    }
    internal class SharpFilter : MatrixFilter
    {
        public SharpFilter() {
            kernel = new float[3, 3]
            {
                {0, -1, 0 },
                {-1, 5, -1 },
                {0, -1, 0 }
            };
        }
    }
}
