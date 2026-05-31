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
        private static string LogPath = "";

        [STAThread]
        private static int Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);

            try
            {
                string source = AppDomain.CurrentDomain.BaseDirectory.TrimEnd(Path.DirectorySeparatorChar);
                LogPath = Path.Combine(source, "TCToRPG-Client-launcher.log");
                Log("Launcher started from " + source);
                string prism = FindPrismLauncher(source);
                Log("Prism path: " + (prism ?? "<not found>"));
                if (prism == null)
                {
                    MessageBox.Show(
                        "Prism Launcher was not found.\n\nInstall Prism Launcher or place prismlauncher.exe next to this launcher.",
                        "TCToRPG Client",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Warning);
                    TryOpen("https://prismlauncher.org/download/");
                    return 1;
                }

                string appData = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
                string instances = Path.Combine(appData, "PrismLauncher", "instances");
                string target = Path.Combine(instances, InstanceName);
                Log("Instance target: " + target);

                Directory.CreateDirectory(instances);
                StopExistingPrism(prism);
                CopyDirectory(source, target, new HashSet<string>(StringComparer.OrdinalIgnoreCase)
                {
                    "dist",
                    ".gradle",
                    "build",
                    "logs",
                    "crash-reports",
                    "minecraft",
                    "resourcepacks"
                });

                EnsureRuntimeFiles(target);
                Log("Launching Prism.");
                LaunchPrism(prism);
                Log("Launcher finished.");
                return 0;
            }
            catch (Exception ex)
            {
                MessageBox.Show(
                    ex.Message,
                    "TCToRPG launch failed",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error);
                return 1;
            }
        }

        private static void LaunchPrism(string prism)
        {
            string workingDirectory = Path.GetDirectoryName(prism) ?? "";
            using (Process launched = Process.Start(new ProcessStartInfo
            {
                FileName = prism,
                Arguments = "--launch \"" + InstanceName + "\"",
                UseShellExecute = true,
                WorkingDirectory = workingDirectory
            }))
            {
                if (launched == null)
                {
                    throw new InvalidOperationException("Prism Launcher process could not be started.");
                }

                if (launched.WaitForExit(30000))
                {
                    Log("Prism exited quickly with code: " + launched.ExitCode);
                    LaunchPrismFallback(prism, workingDirectory);

                    MessageBox.Show(
                        "Prism Launcher started, but automatic TCToRPG launch did not stay open.\n\n" +
                        "Prism was opened normally. Select the TCToRPG-Client instance and press Launch.\n\n" +
                        "Log: " + LogPath,
                        "TCToRPG Client",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Information);
                }
            }
        }

        private static void LaunchPrismFallback(string prism, string workingDirectory)
        {
            string shortcut = FindPrismShortcut();
            string fileName = File.Exists(shortcut) ? shortcut : prism;
            Log("Launching Prism fallback: " + fileName);
            Process.Start(new ProcessStartInfo
            {
                FileName = fileName,
                UseShellExecute = true,
                WorkingDirectory = workingDirectory
            });
        }

        private static string FindPrismShortcut()
        {
            string appData = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
            string commonStartMenu = Environment.GetFolderPath(Environment.SpecialFolder.CommonStartMenu);
            string userStartMenu = Path.Combine(appData, "Microsoft", "Windows", "Start Menu", "Programs");

            string[] candidates =
            {
                Path.Combine(userStartMenu, "Prism Launcher.lnk"),
                Path.Combine(commonStartMenu, "Programs", "Prism Launcher.lnk")
            };

            foreach (string candidate in candidates)
            {
                if (File.Exists(candidate))
                {
                    return candidate;
                }
            }

            return null;
        }

        private static void StopExistingPrism(string prism)
        {
            try
            {
                string prismFullPath = Path.GetFullPath(prism);
                foreach (Process process in Process.GetProcessesByName("prismlauncher"))
                {
                    string processPath = "";
                    try
                    {
                        processPath = process.MainModule.FileName;
                    }
                    catch
                    {
                    }

                    if (!string.Equals(processPath, prismFullPath, StringComparison.OrdinalIgnoreCase))
                    {
                        continue;
                    }

                    Log("Stopping existing Prism process: " + process.Id);
                    try
                    {
                        if (!process.CloseMainWindow())
                        {
                            process.Kill();
                        }
                        if (!process.WaitForExit(3000))
                        {
                            process.Kill();
                            process.WaitForExit(3000);
                        }
                    }
                    catch (Exception ex)
                    {
                        Log("Failed to stop Prism process " + process.Id + ": " + ex.Message);
                    }
                }
            }
            catch (Exception ex)
            {
                Log("Prism process cleanup skipped: " + ex.Message);
            }
        }

        private static string FindPrismLauncher(string source)
        {
            string localAppData = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData);
            string programFiles = Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles);
            string programFilesX86 = Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86);

            string[] candidates =
            {
                Path.Combine(localAppData, "Programs", "PrismLauncher", "prismlauncher.exe"),
                Path.Combine(programFiles, "PrismLauncher", "prismlauncher.exe"),
                Path.Combine(programFilesX86, "PrismLauncher", "prismlauncher.exe"),
                Path.Combine(source, "PrismLauncher", "prismlauncher.exe"),
                Path.Combine(source, "prismlauncher.exe")
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

            CopyIfExists(Path.Combine(instanceRoot, "mods", "tctorpg.jar"), Path.Combine(gameDir, "mods", "tctorpg.jar"));
            CopyIfExists(
                Path.Combine(AppDomain.CurrentDomain.BaseDirectory.TrimEnd(Path.DirectorySeparatorChar), "resourcepacks", "TCToRPG-Resources.zip"),
                Path.Combine(instanceRoot, "resourcepacks", "TCToRPG-Resources.zip"));
            CopyIfExists(
                Path.Combine(instanceRoot, "resourcepacks", "TCToRPG-Resources.zip"),
                Path.Combine(gameDir, "resourcepacks", "TCToRPG-Resources.zip"));

            string options = Path.Combine(gameDir, "options.txt");
            string resourceLine = "resourcePacks:[\"vanilla\",\"file/TCToRPG-Resources.zip\"]";
            try
            {
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
                Log("Options updated.");
            }
            catch (Exception ex)
            {
                Log("Options update skipped: " + ex.Message);
            }
            Log("Runtime files prepared.");
        }

        private static void CopyDirectory(string source, string target, HashSet<string> excludedNames)
        {
            Directory.CreateDirectory(target);

            foreach (string directory in Directory.GetDirectories(source))
            {
                string name = Path.GetFileName(directory);
                if (excludedNames.Contains(name))
                {
                    Log("Skipped directory: " + directory);
                    continue;
                }

                CopyDirectory(directory, Path.Combine(target, name), excludedNames);
            }

            foreach (string file in Directory.GetFiles(source))
            {
                string name = Path.GetFileName(file);
                if (ShouldSkipRootFile(name))
                {
                    Log("Skipped root file: " + file);
                    continue;
                }

                CopyIfExists(file, Path.Combine(target, name));
            }
        }

        private static bool ShouldSkipRootFile(string name)
        {
            string[] skipped =
            {
                "TCToRPG-Client.exe",
                "TCToRPG-Client-launcher.log",
                "build-release.cmd",
                "build-release.sh",
                "packwiz-refresh.cmd",
                "packwiz-refresh.sh",
                "packwiz-update.cmd",
                "packwiz-update.sh"
            };

            return skipped.Contains(name, StringComparer.OrdinalIgnoreCase);
        }

        private static void CopyIfExists(string source, string destination)
        {
            if (!File.Exists(source))
            {
                return;
            }

            Directory.CreateDirectory(Path.GetDirectoryName(destination));
            try
            {
                Log("Copying: " + source + " -> " + destination);
                File.Copy(source, destination, true);
                Log("Copied: " + source + " -> " + destination);
            }
            catch (IOException ex)
            {
                Log("Copy skipped because the file is busy: " + destination + " / " + ex.Message);
            }
            catch (UnauthorizedAccessException ex)
            {
                Log("Copy skipped because access was denied: " + destination + " / " + ex.Message);
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

        private static void Log(string message)
        {
            try
            {
                if (string.IsNullOrWhiteSpace(LogPath))
                {
                    return;
                }

                File.AppendAllText(
                    LogPath,
                    "[" + DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss") + "] " + message + Environment.NewLine);
            }
            catch
            {
            }
        }
    }
}
