package reobf.proghatches.main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.Configuration;
import reobf.proghatches.lang.LangManager;

import gregtech.api.util.GT_Multiblock_Tooltip_Builder;

public class Config {
	public static boolean appendAddedBy = true;
	public static String greeting = "Hello World";
	public static int metaTileEntityOffset = 22000;
	public static boolean skipRecipeAdding;
	public static boolean debug;
	public static boolean dev;
	public static boolean experimentalOptimize=true;
	public static boolean sleep=true;
	public static void synchronizeConfiguration(File configFile) {
		Configuration configuration = new Configuration(configFile);

		metaTileEntityOffset = configuration.getInt("MetaTEOffset", "ID", metaTileEntityOffset, 14301, 29999,
				"The GT MetaTE ID used by this mod, will use range:[offset,offset+200], make sure it's in [14301,14999] or [17000,29999]");
		configuration.addCustomCategoryComment("ID", "Configurable ID settings, DO NOT change it until necessary.");
		skipRecipeAdding = configuration.getBoolean("skipRecipeAddition", Configuration.CATEGORY_GENERAL,
				skipRecipeAdding, "If true, this mod will not add any recipe.");
		appendAddedBy = configuration.getBoolean("appendAddedBy", Configuration.CATEGORY_GENERAL, appendAddedBy,
				"Append 'Added by ProgrammableHatches' at the end of machine desc.");
		debug = configuration.getBoolean("debug", Configuration.CATEGORY_GENERAL, debug,
				"Allow you see some technical info in waila, for debugging.");
		experimentalOptimize = configuration.getBoolean("Piority mode","Experimental", experimentalOptimize,
				"When on, buffer with more copies gets handled first. If off, just handle the first non-empty buffer first.");
		sleep = configuration.getBoolean("Hatch sleep mode", "Experimental", sleep,
				"When on, hatch will sleep when not busy, to ease server load.");
		
		
		if (configuration.hasChanged()) {
			configuration.save();
		}
		dev=(Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
	}

	public static String lang = System.getProperty("user.language").equalsIgnoreCase("zh") ? "zh_CN" : "en_US";

	public static void get(GT_Multiblock_Tooltip_Builder obj, String key, boolean defaulted) {
		try (InputStream in = (defaulted ? getInputEN : getInput).apply(key)) {
			if (in == null) {
				if (defaulted) {
					obj.addMachineType("!!!error!!! failed to translate")
							.toolTipFinisher("do not remove en_US folder!");
					return;
				}
				get(obj, key, true);
				return;
			}
			byte[] b = new byte[in.available()];
			int off = 0;
			int tmp;
			do {
				tmp = in.read(b, off, b.length - off);
				off += tmp;
			} while (in.available() > 0);
			String[] arr = new String(b, "UTF-8").split("\r?\n");
			for (String str : arr) {
				int a0 = str.indexOf("(");
				int a1 = str.lastIndexOf(")->");
				String func = str.substring(0, a0);
				String args = str.substring(a0 + 1, a1);
				String type = str.substring(a1 + 1 + 2, str.length());
				MethodType tp = MethodType.fromMethodDescriptorString(

						type + (func.equals("toolTipFinisher") ? "V"
								: "Lgregtech/api/util/GT_Multiblock_Tooltip_Builder;")

						, Config.class.getClassLoader());
				call(obj, args, MethodHandles.lookup().findVirtual(GT_Multiblock_Tooltip_Builder.class, func, tp), tp);

			}

		} catch (Exception e) {
			MyMod.LOG.fatal("failed to get GT description:" + key);
			e.printStackTrace();
		}

		if (Config.appendAddedBy)
			obj.toolTipFinisher(LangManager.translateToLocal("programmable_hatches.addedby"));
		;
	}

	private static void call(Object callee, String args, MethodHandle virtual, MethodType type) {
		Object[] topass = new Object[type.parameterArray().length + 1];
		LinkedList<String> args0 = new LinkedList<>();
		Arrays.stream(args.split("¶")).forEach(args0::add);
		;
		int index = 0;
		topass[index++] = callee;
		for (Class<?> c : type.parameterArray()) {
			if (c == String.class) {
				topass[index++] = args0.pop();
			} else if (c == int.class) {
				topass[index++] = Integer.parseInt(args0.pop());
			} else if (c == int[].class) {
				int[] a = new int[args0.size()];
				for (int i = 0; i < a.length; i++) {
					a[i] = Integer.parseInt(args0.pop());
				}
				topass[index++] = a;
			} else if (c == boolean.class) {
				topass[index++] = Boolean.parseBoolean(args0.pop());

			} else {
				throw new RuntimeException("cannot parse" + c);
			}
		}
		try {
			virtual = virtual.asFixedArity();
			virtual.invokeWithArguments(topass);
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	public static void get(GT_Multiblock_Tooltip_Builder obj, String key) {
		get(obj, key, false);
	}

	public static String[] get(String key, Map<String, Object> fmtter) {
		return get(key, fmtter, false);
	}

	public static String[] get(String key, Map<String, Object> fmtter, boolean defaulted) {

		if (fmtter.containsKey("int format")) {
			fmtter = new HashMap<>(fmtter);
			Object optfmt = fmtter.remove("int format");
			if (optfmt != null) {
				java.text.DecimalFormat format = new java.text.DecimalFormat(optfmt.toString());
				fmtter.replaceAll((k, v) -> {
					if (v instanceof Integer) {

						return format.format(((Integer) v).intValue());
					}

					return v;
				});
			}
		}

		try (InputStream in = (defaulted ? getInputEN : getInput).apply(key)) {
			if (in == null) {
				if (defaulted)
					return new String[] { "!!! FATAL !!!", "en_US language file missing:" + key + ".lang",
							"do not remove en_US folder!",

					};

				return get(key, fmtter, true);
			}
			byte[] b = new byte[in.available()];
			int off = 0;
			int tmp;
			do {
				tmp = in.read(b, off, b.length - off);
				off += tmp;
			} while (in.available() > 0);
			String[] arr = new String(b, "UTF-8").split("\r?\n");
			for (int i = 0; i < arr.length; i++) {
				final int ii = i;
				final String[] arrf = arr;
				String oldstr="";
				do{ oldstr=arr[ii];
				fmtter.forEach((k, v) -> {
					arrf[ii] = arrf[ii].replace(String.format("{%s}", k), v.toString());
					// arr[ii].inde

					Pattern p = Pattern.compile("\\{%s\\?\\}.*?\\{%s:\\}.*?\\{%s!\\}".replace("%s", k));
					while (true) {
						Matcher m = p.matcher(arrf[ii]);
						if (m.find()) {
							String torep = arrf[ii].substring(m.start(), m.end());
							String repby;
							int ia = torep.indexOf("{" + k + "?}");
							int ib = torep.indexOf("{" + k + ":}");
							int ic = torep.indexOf("{" + k + "!}");
							if (Boolean.valueOf(v.toString())) {
								repby = torep.substring(ia + ("{" + k + "?}").length(), ib);
							} else {
								repby = torep.substring(ib + ("{" + k + ":}").length(), ic);
							}

							arrf[ii] = arrf[ii].replace(torep, repby);
						} else {
							break;
						}
					}

				});
				
				}while(!oldstr.equals(arrf[ii]));//if something is changed, check it again

			}
			// System.out.println(Arrays.asList(arr));

			if (appendAddedBy) {
				String[] t = arr;
				t = new String[arr.length + 1];
				System.arraycopy(arr, 0, t, 0, arr.length);
				t[t.length - 1] = LangManager.translateToLocal("programmable_hatches.addedby");
				arr = t;
			}
			if (defaulted) {

				MyMod.LOG.fatal("Your current translation key:'programmable_hatches.gt.lang.dir' maps to:"
						+ LangManager.translateToLocal("programmable_hatches.gt.lang.dir"));
				MyMod.LOG.fatal("That means yous should put translated " + key + ".lang in "
						+ "/assets/proghatches/lang/" + LangManager.translateToLocal("programmable_hatches.gt.lang.dir")
						+ "/" + key + ".lang");
				MyMod.LOG.fatal("... then delete GregTech.lang to regenerate that file.");

			}

			return arr;

		} catch (IOException e) {
			MyMod.LOG.fatal("failed to get GT description:" + key);
			e.printStackTrace();
		}

		return null;
	}

	static Function<String, InputStream> getInputEN = s -> Config.class
			.getResourceAsStream("/assets/proghatches/lang/en_US/" + s + ".lang");
	static Function<String, InputStream> getInput = s -> Config.class.getResourceAsStream("/assets/proghatches/lang/"
			+ LangManager.translateToLocal("programmable_hatches.gt.lang.dir") + "/" + s + ".lang");
	
	

}
