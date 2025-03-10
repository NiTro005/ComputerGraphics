using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace Lab_1
{
    public partial class Form1 : Form
    {
        Bitmap image;
        Bitmap orig_image;
        public Form1()
        {
            InitializeComponent();
        }

        private void фильтрыToolStripMenuItem_Click(object sender, EventArgs e)
        {

        }

        private void открытьToolStripMenuItem_Click(object sender, EventArgs e)
        {
            OpenFileDialog dialog = new OpenFileDialog();
            if(dialog.ShowDialog() == DialogResult.OK)
            {
                image = new Bitmap(dialog.FileName);
                pictureBox1.Image = image;
                orig_image = image;
                pictureBox1.Refresh();
            }
            
        }

        private void инверсияToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Filters inverse = new InverseFilter();
            backgroundWorker1.RunWorkerAsync(inverse);
        }

        private void button1_Click(object sender, EventArgs e)
        {
            backgroundWorker1.CancelAsync();
        }

        private void backgroundWorker1_DoWork(object sender, DoWorkEventArgs e)
        {
            Bitmap newImage = ((Filters)e.Argument).process_image(image, backgroundWorker1);
            if( backgroundWorker1.CancellationPending != true)
            {
                image = newImage;
            }
        }

        private void backgroundWorker1_ProgressChanged(object sender, ProgressChangedEventArgs e)
        {
            progressBar1.Value = e.ProgressPercentage;
        }

        private void backgroundWorker1_RunWorkerCompleted(object sender, RunWorkerCompletedEventArgs e)
        {
            if (!e.Cancelled) {
                pictureBox1.Image = image;
                pictureBox1.Refresh();
            }
            progressBar1.Value = 0;
        }

        private void блюрToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Filters fil= new BlurFilter();
            backgroundWorker1.RunWorkerAsync(fil);

        }

        private void фильтрГаусаToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Filters fil = new GaussianFilter();
            backgroundWorker1.RunWorkerAsync(fil);
        }

        private void оттенкиСерогоToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Filters fil = new GrayScaleFilter();
            backgroundWorker1.RunWorkerAsync(fil);
        }

        private void сепияToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Filters fil = new SepiaFilter();
            backgroundWorker1.RunWorkerAsync(fil);
        }

        private void яркостьToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Filters fil = new BrightFilter();
            backgroundWorker1.RunWorkerAsync(fil);
        }

        private void фильтрСобеляToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Filters fil = new SobelFilter();
            backgroundWorker1.RunWorkerAsync(fil);
        }

        private void резкостьToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Filters fil = new SharpFilter();
            backgroundWorker1.RunWorkerAsync(fil);
        }

        private void button2_Click(object sender, EventArgs e)
        {
            image = orig_image;
            if (trackBar1.Value != 0)
            {
                Filters fil = new BrightFilter(trackBar1.Value);
                backgroundWorker1.RunWorkerAsync(fil);
            }
            if (trackBar3.Value != 0)
            {
                Filters fil = new ContrastFilter((double)trackBar3.Value/10);
                backgroundWorker1.RunWorkerAsync(fil);
            }
        }

        private void button3_Click(object sender, EventArgs e)
        {
            pictureBox1.Image = orig_image;
            image = orig_image;
            pictureBox1.Refresh();
        }

        private void сдвигToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Filters fil = new ShiftFilter();
            backgroundWorker1.RunWorkerAsync(fil);
        }

        private void тиснениеToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Filters fil = new EmbossFilter();
            backgroundWorker1.RunWorkerAsync(fil);
        }

        private void серыйМирToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Filters fil = new GrayWorldFilter(image);
            backgroundWorker1.RunWorkerAsync(fil);
        }

        private void autolevelToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Filters fil = new AutolevelFilter(image);
            backgroundWorker1.RunWorkerAsync(fil);
        }

        private void вертикальноToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Filters fil = new MirrorFilter(MirrorFilter.MirrorType.Vertical);
            backgroundWorker1.RunWorkerAsync(fil);
        }

        private void горизонтальноToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Filters fil = new MirrorFilter(MirrorFilter.MirrorType.Horizontal);
            backgroundWorker1.RunWorkerAsync(fil);
        }

        private void расширениеToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Filters fil = new DilationFilter();
            backgroundWorker1.RunWorkerAsync(fil);
        }

        private void сужениеToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Filters fil = new ErosionFilter();
            backgroundWorker1.RunWorkerAsync(fil);
        }

        private void медианныйФильтрToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Filters fil = new MedianFilter();
            backgroundWorker1.RunWorkerAsync(fil);
        }

        private void фильтрЩарраToolStripMenuItem1_Click(object sender, EventArgs e)
        {
            Filters fil = new ScharrFilter();
            backgroundWorker1.RunWorkerAsync(fil);
        }
    }
}
