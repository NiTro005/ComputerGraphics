using System.Drawing;
using System;

internal class TransferHelper
{
    private static int min = -3000;
    private static int max = 16000;

    public static void SetTF(int newMin, int newMax)
    {
        min = newMin;
        max = newMax;
    }

    public static Color TransferFunction(short value)
    {
        int newVal = Math.Max(0, Math.Min((value - min) * 255 / (max - min), 255));
        return Color.FromArgb(255, newVal, newVal, newVal);
    }
}
