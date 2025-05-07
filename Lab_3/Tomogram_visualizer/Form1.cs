using OpenTK.Graphics.OpenGL;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using OpenTK;
using OpenTK.Platform;

namespace Tomogram_visualizer
{
    public partial class Form1 : Form
    {
        bool loaded = false;
        int currentLayer = 0;
        Bin bin = new Bin();
        private View view = new View();

        int minTF = -3000;
        int widthTF = 2000;

        int FrameCount;
        DateTime NextFPSUpdate = DateTime.Now.AddSeconds(1);
        void displayFPS()
        {
            if (DateTime.Now >= NextFPSUpdate)
            {
                this.Text = String.Format("CT Visualizer (fps = {0})", FrameCount);
                NextFPSUpdate = DateTime.Now.AddSeconds(1);
                FrameCount = 0;
            }
            FrameCount++;
        }

        public Form1()
        {
            InitializeComponent();
        }


        private void Form1_Load(object sender, EventArgs e)
        {
            Application.Idle += Application_Idle;
        }


        private void открытьToolStripMenuItem_Click(object sender, EventArgs e)
        {
            OpenFileDialog dialog = new OpenFileDialog();

            if (dialog.ShowDialog() == DialogResult.OK)
            {
                string str = dialog.FileName;
                bin.readBIN(str);
                view.SetupView(glControl1.Width, glControl1.Height);
                loaded = true;
                glControl1.Invalidate();
                trackBar1.Maximum = Bin.Z - 1;

                trackBarMin.Minimum = -3000;
                trackBarMin.Maximum = 16000;
                trackBarMin.Value = minTF;

                trackBarWidth.Minimum = 1;
                trackBarWidth.Maximum = 2000;
                trackBarWidth.Value = widthTF;
            }
        }

        void Application_Idle(object sender, EventArgs е)
        {
            while (glControl1.IsIdle)
            {
                displayFPS();
                glControl1.Invalidate();
            }

        }

        bool needReload = false;

        private void glControl1_Paint(object sender, PaintEventArgs e)
        {

            if (loaded)
            {
                if (checkBox1.Checked == true)
                {
                    // view.DrawQuads(currentLayer);
                    if (needReload)
                    {
                        view.GenerateTextureImage(currentLayer);
                        view.Load2DTexture();
                        needReload = false;
                    }

                    view.DrawTexture();
                    glControl1.SwapBuffers();
                }
                else if (radioButtonImproved.Checked == true)
                {
                    view.DrawQuadStrip(currentLayer);
                    glControl1.SwapBuffers();
                }
                else 
                {
                    view.DrawQuads(currentLayer);
                    glControl1.SwapBuffers();
                }
            }
        }

        private void trackBar1_Scroll_1(object sender, EventArgs e)
        {
            currentLayer = trackBar1.Value;
            needReload = true;
            //glControl1.Invalidate();

        }

        private void checkBox1_CheckedChanged(object sender, EventArgs e)
        {

        }

        private void trackBarMin_Scroll(object sender, EventArgs e)
        {
            minTF = trackBarMin.Value;
            widthTF = trackBarWidth.Value;
            TransferHelper.SetTF(minTF, minTF + widthTF);
            needReload = true;
            glControl1.Invalidate();
        }

        private void trackBarWidth_Scroll(object sender, EventArgs e)
        {
            widthTF = trackBarWidth.Value;
            TransferHelper.SetTF(minTF, minTF + widthTF);
            needReload = true;
            glControl1.Invalidate();
        }

        private void glControl1_Load(object sender, EventArgs e)
        {

        }
    }
}
