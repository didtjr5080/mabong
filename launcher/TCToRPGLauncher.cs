using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Windows.Forms;

namespace TCToRPGLauncher
{
    internal static class Program
    {
        private const string InstanceName = "TCToRPG-Client";

        [STAThread]
        private static int Main()
        {
            try
            {
                string source = AppDomain.CurrentDomain.BaseDirectory.TrimEnd(Path.DirectorySeparatorChar);
                string prism = FindPrismLauncher();
                if (prism == null)
                {
                    MessageBox.Show(
                        "Prism Launcher가 설치되어 있지 않습니다.\n\nPrism Launcher를 설치한 뒤 이 실행 파일을 다시 실행하세요.",
                        "TCToRPG 전용 클라이언트",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Warning);
                    TryOpen("https://prismlauncher.org/download/");
                    return 1;
                }

                string appData = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
                string instances = Path.Combine(appData, "PrismLauncher", "instances");
                string target = Path.Combine(instances, InstanceName);

                Directory.CreateDirectory(instances);
                CopyDirectory(source, target, new HashSet<string>(StringComparer.OrdinalIgnoreCase)
                {
                    "dist",
                    ".gradle",
                    "build"
                });

                EnsureRuntimeFiles(target);

                var start = new ProcessStartInfo
                {
                    FileName = prism,
                    Arguments = "--launch \"" + InstanceName + "\"",
                    UseShellExecute = true,
                    WorkingDirectory = Path.GetDirectoryName(prism)
                };
                Process.Start(start);
                return 0;
            }
            catch (Exception ex)
            {
                MessageBox.Show(
                    ex.Message,
                    "TCToRPG 실행 실패",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error);
                return 1;
            }
        }

        private static string FindPrismLauncher()
        {
            string[] candidates =
            {
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "Programs", "PrismLauncher", "prismlauncher.exe"),
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles), "PrismLauncher", "prismlauncher.exe"),
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86), "PrismLauncher", "prismlauncher.exe"),
                Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "PrismLauncher", "prismlauncher.exe"),
                Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "prismlauncher.exe")
            };

            foreach (string candidate in candidates)
            {
                if (File.Exists(candidate))
                {
                    return candidate;
                }
            }

            string fromPath = FindOnPath("prismlauncher.exe");
            return File.Exists(fromPath) ? fromPath : null;
        }

        private static string FindOnPath(string fileName)
        {
            string path = Environment.GetEnvironmentVariable("PATH") ?? "";
            foreach (string dir in path.Split(Path.PathSeparator))
            {
                if (string.IsNullOrWhiteSpace(dir))
                {
                    continue;
                }

                string candidate = Path.Combine(dir.Trim(), fileName);
                if (File.Exists(candidate))
                {
                    return candidate;
                }
            }

            return null;
        }

        private static void EnsureRuntimeFiles(string instanceRoot)
        {
            string gameDir = Path.Combine(instanceRoot, "minecraft");
            Directory.CreateDirectory(Path.Combine(gameDir, "mods"));
            Directory.CreateDirectory(Path.Combine(gameDir, "resourcepacks"));

            string rootMod = Path.Combine(instanceRoot, "mods", "tctorpg.jar");
            string gameMod = Path.Combine(gameDir, "mods", "tctorpg.jar");
            if (File.Exists(rootMod))
            {
                File.Copy(rootMod, gameMod, true);
            }

            string rootPack = Path.Combine(instanceRoot, "resourcepacks", "TCToRPG-Resources.zip");
            string gamePack = Path.Combine(gameDir, "resourcepacks", "TCToRPG-Resources.zip");
            if (File.Exists(rootPack))
            {
                File.Copy(rootPack, gamePack, true);
            }

            string options = Path.Combine(gameDir, "options.txt");
            string resourceLine = "resourcePacks:[\"vanilla\",\"file/TCToRPG-Resources.zip\"]";
            var lines = File.Exists(options) ? File.ReadAllLines(options).ToList() : new List<string>();
            int index = lines.FindIndex(line => line.StartsWith("resourcePacks:", StringComparison.Ordinal));
            if (index >= 0)
            {
                lines[index] = resourceLine;
            }
            else
            {
                lines.Add(resourceLine);
            }
            File.WriteAllLines(options, lines);
        }

        private static void CopyDirectory(string source, string target, HashSet<string> excludedNames)
        {
            Directory.CreateDirectory(target);

            foreach (string directory in Directory.GetDirectories(source))
            {
                string name = Path.GetFileName(directory);
                if (excludedNames.Contains(name))
                {
                    continue;
                }

                CopyDirectory(directory, Path.Combine(target, name), excludedNames);
            }

            foreach (string file in Directory.GetFiles(source))
            {
                string name = Path.GetFileName(file);
                string destination = Path.Combine(target, name);
                File.Copy(file, destination, true);
            }
        }

        private static void TryOpen(string url)
        {
            try
            {
                Process.Start(new ProcessStartInfo
                {
                    FileName = url,
                    UseShellExecute = true
                });
            }
            catch
            {
            }
        }
    }
}
