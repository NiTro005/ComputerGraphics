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
            throw new NotImplementedException();
        }

    }
}
