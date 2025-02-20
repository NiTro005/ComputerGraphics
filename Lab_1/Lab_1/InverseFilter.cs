using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Drawing;

namespace Lab_1
{
    internal class InverseFilter : Filters
    {
        protected override Color calculateNewPixelColor(Bitmap sourceImage, int x, int y)
        {
            Color source_color = sourceImage.GetPixel(x, y);
            Color new_color = Color.FromArgb(255 - source_color.R, 255 - source_color.G, 255 -source_color.B);
            return new_color;
        }
    }
}
