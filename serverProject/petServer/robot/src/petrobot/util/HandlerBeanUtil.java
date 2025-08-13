package petrobot.util;

import java.io.File;
import java.io.FileFilter;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class HandlerBeanUtil {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> Set<Class<T>> getSubClasses(String packageName, Class<T> parentClass) {
		Set ret = new LinkedHashSet();
		String packageDirName = packageName.replace('.', '/');
		try {
			Enumeration dirs = HandlerBeanUtil.class.getClassLoader().getResources(packageDirName);
			while (dirs.hasMoreElements()) {
				URL url = (URL)dirs.nextElement();
				String protocol = url.getProtocol();
				if ("file".equals(protocol)) {
					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
					ret.addAll(findSubClazzFromFile(packageName, filePath, parentClass));
				} else if ("jar".equals(protocol)) {
					JarFile jar = ((JarURLConnection)url.openConnection()).getJarFile();
					ret.addAll(findSubClassFromJar(jar, packageName, packageDirName, parentClass));
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return ret;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> Set<Class<T>> findSubClassFromJar(JarFile jar, String packageName, String packageDirName, Class<T> parentClass)
	{
		Set ret = new LinkedHashSet();
		Enumeration entries = jar.entries();
		while (entries.hasMoreElements()) {
			JarEntry entry = (JarEntry)entries.nextElement();
			String name = entry.getName();
			if (name.charAt(0) == '/') {
				name = name.substring(1);
			}
			String packageNameNew = packageName;
			if (!(name.startsWith(packageDirName))) {
				continue;
			}

			int idx = name.lastIndexOf(47);
			if (idx != -1) {
				packageNameNew = name.substring(0, idx).replace('/', '.');
			}
			if (((idx == -1)) || 
					(!(name.endsWith(".class"))) || (entry.isDirectory())) continue;
			String className = name.substring(packageNameNew.length() + 1, name.length() - 6);
			if (!(className.endsWith("Handler")))
				continue;
			try
			{
				Class loadClass = Class.forName(packageNameNew + '.' + className, false, 
						HandlerBeanUtil.class.getClassLoader());
				if ((parentClass.isAssignableFrom(loadClass)) && (!(parentClass.equals(loadClass))))
					ret.add(loadClass);
			}
			catch (Throwable e) {
				e.printStackTrace();
			}

		}

		return ret;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> Set<Class<T>> findSubClazzFromFile(String packageName, String packagePath, Class<T> parentClass)
	{
		File dir = new File(packagePath);
		if ((!(dir.exists())) || (!(dir.isDirectory()))) {
			return Collections.emptySet();
		}

		Set ret = new LinkedHashSet();
		File[] dirfiles = dir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return ((file.isDirectory()) || (file.getName().endsWith(".class")));
			}
		});
		for (File file : dirfiles) {
			if (file.isDirectory()) {
				ret.addAll(findSubClazzFromFile(packageName + "." + file.getName(), file.getAbsolutePath(),parentClass));
			} else {
				String className = file.getName().substring(0, file.getName().length() - 6);
				if (!(className.endsWith("Handler")))
					continue;
				try
				{
					Class clazz = Class.forName(packageName + '.' + className, false, 
							HandlerBeanUtil.class.getClassLoader());
					if ((parentClass.isAssignableFrom(clazz)) && (!(parentClass.equals(clazz))))
						ret.add(clazz);
				}
				catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}
}
