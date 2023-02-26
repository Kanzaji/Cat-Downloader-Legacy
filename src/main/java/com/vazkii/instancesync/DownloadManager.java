package com.vazkii.instancesync;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.kanzaji.catdownloader.jsons.Manifest;
import com.kanzaji.catdownloader.jsons.Manifest.Files;
import com.kanzaji.catdownloader.utils.Logger;

public class DownloadManager {

	private final Path modsDir;
	
	private List<String> acceptableFilenames = new LinkedList<>();
	private ExecutorService executor; 
	private int downloadCount;

	private Logger logger = Logger.getInstance();
	
	public DownloadManager(Path modsDir) {
		this.modsDir = modsDir;
	}

	public void downloadInstance(Manifest manifest) {
		executor = Executors.newFixedThreadPool(16);

		System.out.println("Downloading mods...");
		logger.log("Starting downloading mods...");
		long time = System.currentTimeMillis();
		int failed = 0;

		for(Files a : manifest.files) 
			if(a.getData(manifest.minecraft)) {downloadAddonIfNeeded(a);} else {failed += 1;}

		if(downloadCount == 0) {
			if (failed > 0) {
				System.out.println(failed + " mods failed to download! Check the log at \"" + logger.getLogPath() + "\" for more details!");
				logger.error(failed + " mods failed to download. Look for errors in the log for more details!");
			} else {
				System.out.println("No mods need to be downloaded, yay!");
				logger.log("No mods need to be downloaded.");
			}
		} else try {
			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.DAYS);

			float secs = (float) (System.currentTimeMillis() - time) / 1000F;
			logger.log(String.format("Finished downloading %d mods (Took %.2fs)", downloadCount, secs));
			System.out.printf("Finished downloading %d mods (Took %.2fs)%n", downloadCount, secs);
			if (failed > 0) {
				System.out.println(failed + " mods failed to download! Check the log at \"" + logger.getLogPath() + "\" for more details!");
				logger.error(failed + " mods failed to download. Look for errors in the log for more details!");
			}
		} catch (InterruptedException e) {
			System.out.println("Downloads were interrupted!");
			logger.logStackTrace("Downloads were interrupted!", e);
		}

		// deleteRemovedMods();
	}

	private void downloadAddonIfNeeded(Files addon) {
		
		String filenameOnDisk = addon.getFileName();
		acceptableFilenames.add(filenameOnDisk);

		Path modFile = Path.of(modsDir.toAbsolutePath().toString(), filenameOnDisk);
		if(!modExists(modFile)) {
			download(modFile, addon.downloadUrl);
		}
	}

	private void download(final Path target, final String downloadUrl) {
		Runnable run = () -> {
			String name = target.getFileName().toString();

			try {
				logger.log("Downloading " + name);
				long time = System.currentTimeMillis(); 

				URL url = new URL(downloadUrl);
				OutputStream out = java.nio.file.Files.newOutputStream(target, StandardOpenOption.CREATE_NEW);

				URLConnection connection = url.openConnection();
				InputStream in = connection.getInputStream();

				byte[] buf = new byte[4096];
				int read;

				while((read = in.read(buf)) > 0)
					out.write(buf, 0, read);

				out.close();
				in.close();

				float secs = (float) (System.currentTimeMillis() - time) / 1000F;
				logger.log(String.format("Finished downloading %s (Took %.2fs)", name, secs));
			} catch(Exception e) {
				System.out.println("Failed to download " + name);
				logger.logStackTrace("Failed to download " + name, e);
			}

			try {
				if (java.nio.file.Files.size(target) == 0) {
					logger.warn("Probably failed to download " + name +" // File appears to be empty!");
					logger.warn("Trying to download " + name +" again...");
					if (java.nio.file.Files.deleteIfExists(target))	download(target, downloadUrl);
				}
			} catch (IOException e) {
				logger.logStackTrace("Failed to delete file " + name, e);
			}
		};

		downloadCount++;
		executor.submit(run);
	}

	private boolean modExists(Path file) {
		if(file.toFile().exists())
			return true;
		
		String name = file.getFileName().toString();
		
		if(name.endsWith(".disabled"))
			return swapIfExists(file, name.replaceAll("\\.disabled", ""));
		else return swapIfExists(file, name + ".disabled");
	}
	
	private boolean swapIfExists(Path target, String searchName) {
		Path search = Path.of(modsDir.toAbsolutePath().toString(), searchName);
		if(search.toFile().exists()) {
			logger.warn("Found alt file for " + target.getFileName() + " -> " + searchName + ", switching filename");
			search.toFile().renameTo(target.toFile());
			return true;
		}
		
		return false;
	}
	
	// private void deleteRemovedMods() {
	// 	System.out.println("Deleting any removed mods");
	// 	File[] files = modsDir.listFiles(f -> !f.isDirectory() && !acceptableFilenames.contains(f.getName()));

	// 	if(files.length == 0)
	// 		System.out.println("No mods were removed, woo!");
	// 	else { 
	// 		for(File f : files) {
	// 			System.out.println("Found removed mod " + f.getName());
	// 			f.delete();
	// 		}
			
	// 		System.out.println("Deleted " + files.length + " old mods");
	// 	}
	// }
}
