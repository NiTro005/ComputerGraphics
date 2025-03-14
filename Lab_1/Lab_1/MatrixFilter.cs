﻿using System;
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

    internal class EmbossFilter : MatrixFilter
    {
        private int brightnessShift;

        public EmbossFilter()
        {
            kernel = new float[,] {
            { 0,  1,  0 },
            { -1, 0,  1 },
            { 0, -1,  0 }
        };
            brightnessShift = 100;
        }

        protected override Color calculateNewPixelColor(Bitmap sourceImage, int x, int y)
        {
            Color baseColor = base.calculateNewPixelColor(sourceImage, x, y);

            int newR = Clamp(baseColor.R + brightnessShift, 0, 255);
            int newG = Clamp(baseColor.G + brightnessShift, 0, 255);
            int newB = Clamp(baseColor.B + brightnessShift, 0, 255);

            int gray = (int)(0.299 * newR + 0.587 * newG + 0.114 * newB);
            gray = Clamp(gray, 0, 255);

            return Color.FromArgb(gray, gray, gray);
        }
    }

    internal class DilationFilter : MatrixFilter
    {
        public DilationFilter()
        {
            kernel = new float[,] {
                { 0, 1, 0 },
                { 1, 1, 1 },
                { 0, 1, 0 }
            };
        }

        protected override Color calculateNewPixelColor(Bitmap sourceImage, int x, int y)
        {
            int radiusX = kernel.GetLength(0) / 2;
            int radiusY = kernel.GetLength(1) / 2;

            int maxR = 0, maxG = 0, maxB = 0;

            for (int i = -radiusX; i <= radiusX; i++)
            {
                for (int j = -radiusY; j <= radiusY; j++)
                {
                    if (kernel[i + radiusX, j + radiusY] == 1)
                    {
                        int Xneib = Clamp(x + j, 0, sourceImage.Width - 1);
                        int Yneib = Clamp(y + i, 0, sourceImage.Height - 1);

                        Color neibColor = sourceImage.GetPixel(Xneib, Yneib);

                        if (neibColor.R > maxR) maxR = neibColor.R;
                        if (neibColor.G > maxG) maxG = neibColor.G;
                        if (neibColor.B > maxB) maxB = neibColor.B;
                    }
                }
            }
            return Color.FromArgb(maxR, maxG, maxB);
        }
    }

    internal class ErosionFilter : MatrixFilter
    {
        public ErosionFilter()
        {
            kernel = new float[,] {
                { 0, 1, 0 },
                { 1, 1, 1 },
                { 0, 1, 0 }
            };
        }

        protected override Color calculateNewPixelColor(Bitmap sourceImage, int x, int y)
        {
            int radiusX = kernel.GetLength(0) / 2;
            int radiusY = kernel.GetLength(1) / 2;

            int minR = 255, minG = 255, minB = 255;

            for (int i = -radiusX; i <= radiusX; i++)
            {
                for (int j = -radiusY; j <= radiusY; j++)
                {
                    if (kernel[i + radiusX, j + radiusY] == 1)
                    {
                        int Xneib = Clamp(x + j, 0, sourceImage.Width - 1);
                        int Yneib = Clamp(y + i, 0, sourceImage.Height - 1);

                        Color neibColor = sourceImage.GetPixel(Xneib, Yneib);

                        if (neibColor.R < minR) minR = neibColor.R;
                        if (neibColor.G < minG) minG = neibColor.G;
                        if (neibColor.B < minB) minB = neibColor.B;
                    }
                }
            }
            return Color.FromArgb(minR, minG, minB);
        }
    }

    internal class ScharrFilter : Filters
    {
        private float[,] kernelX = new float[,]
        {
        { -3, 0, +3 },
        { -10, 0, +10 },
        { -3, 0, +3 }
        };

        private float[,] kernelY = new float[,]
        {
        { -3, -10, -3 },
        { 0, 0, 0 },
        { +3, +10, +3 }
        };

        protected override Color calculateNewPixelColor(Bitmap sourceImage, int x, int y)
        {
            int radiusX = kernelX.GetLength(0) / 2;
            int radiusY = kernelX.GetLength(1) / 2;

            float resultRx = 0, resultGx = 0, resultBx = 0;
            float resultRy = 0, resultGy = 0, resultBy = 0;

            for (int l = -radiusY; l <= radiusY; l++)
            {
                for (int k = -radiusX; k <= radiusX; k++)
                {
                    int idX = Clamp(x + k, 0, sourceImage.Width - 1);
                    int idY = Clamp(y + l, 0, sourceImage.Height - 1);
                    Color neighborColor = sourceImage.GetPixel(idX, idY);

                    resultRx += neighborColor.R * kernelX[k + radiusX, l + radiusY];
                    resultGx += neighborColor.G * kernelX[k + radiusX, l + radiusY];
                    resultBx += neighborColor.B * kernelX[k + radiusX, l + radiusY];

                    resultRy += neighborColor.R * kernelY[k + radiusX, l + radiusY];
                    resultGy += neighborColor.G * kernelY[k + radiusX, l + radiusY];
                    resultBy += neighborColor.B * kernelY[k + radiusX, l + radiusY];
                }
            }

            int gradientR = (int)Math.Sqrt(resultRx * resultRx + resultRy * resultRy);
            int gradientG = (int)Math.Sqrt(resultGx * resultGx + resultGy * resultGy);
            int gradientB = (int)Math.Sqrt(resultBx * resultBx + resultBy * resultBy);

            gradientR = Clamp(gradientR, 0, 255);
            gradientG = Clamp(gradientG, 0, 255);
            gradientB = Clamp(gradientB, 0, 255);

            return Color.FromArgb(gradientR, gradientG, gradientB);
        }
    }
}
