package lt.lb.commons.javafx.scenemanagement;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import java.lang.reflect.Method;
import java.net.URL;
import javafx.stage.Stage;
import javafx.stage.Window;
import lt.lb.commons.Java;
import lt.lb.commons.reflect.unified.ReflMethods;
import lt.lb.fastid.FastIDGen;
import lt.lb.uncheckedutils.PassableException;
import lt.lb.uncheckedutils.SafeOpt;

public class FXWinUtil {

    private static final int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;  // Windows 10 1809+
    private static final SafeOpt NOT_WINDOWS = SafeOpt.error(new PassableException("Not windows"));
    private static final SafeOpt INTERNALS_ERROR = SafeOpt.error(new PassableException("JavaFX class version internals missmatch"));

    public static SafeOpt<WinNT.HRESULT> setDarkMode(Frame frame, boolean dark) {
        if (!isWindows()) {
            return NOT_WINDOWS;
        }

        SafeOpt<WinDef.HWND> handle = frame.getNativeHandle();//implicit cast
        return handle.map(hwnd -> {
            boolean[] darkMode = new boolean[]{dark};

            WinNT.HRESULT result = Dwmapi.INSTANCE.DwmSetWindowAttribute(
                    hwnd,
                    DWMWA_USE_IMMERSIVE_DARK_MODE,
                    darkMode,
                    4 // sizeof(BOOL) = 4 bytes
            );
            return result;
        });

    }
    // JNA interface

    private interface Dwmapi extends WinNT, Library {

        public static Dwmapi INSTANCE = Native.load("dwmapi", Dwmapi.class);

        WinNT.HRESULT DwmSetWindowAttribute(
                WinDef.HWND hwnd,
                int dwAttribute,
                boolean[] pvAttribute,
                int cbAttribute
        );
    }

    private static FastIDGen TITLE_GEN = new FastIDGen(); // ensure unique

    public static SafeOpt<WinDef.HWND> getNativeHandle(Stage frame) {
        SafeOpt<WinDef.HWND> nativeHandleUsingInternals = getNativeHandleUsingInternals(frame);
        if (nativeHandleUsingInternals.isPresent()) {
            return nativeHandleUsingInternals;
        }
        return getNativeHandleUsingJNA(frame);
    }

    public static SafeOpt<WinDef.HWND> getNativeHandleUsingJNA(Stage frame) {// assume javaFX platform thread
        try {

            int currentPid = Kernel32.INSTANCE.GetCurrentProcessId();
            final WinDef.HWND[] result = new WinDef.HWND[1];

            String title = frame.getTitle();
            final boolean useTitleSwap = true;
            String tempTitle = currentPid + "_" + TITLE_GEN.getAndIncrement().toString();
            if (useTitleSwap) {
                frame.setTitle(tempTitle);
            }

            User32.INSTANCE.EnumWindows((hWnd, data) -> {

                // Check process ID
                IntByReference pidRef = new IntByReference();
                User32.INSTANCE.GetWindowThreadProcessId(hWnd, pidRef);

                if (pidRef.getValue() != currentPid) {
                    return true; // continue
                }

                // Get window title
                char[] buffer = new char[512];
                User32.INSTANCE.GetWindowText(hWnd, buffer, 512);
                String wTitle = Native.toString(buffer);

                if (!tempTitle.equals(wTitle)) {
                    return true; // continue
                }

                // ensure it's visible 
                if (!User32.INSTANCE.IsWindowVisible(hWnd)) {
                    return true;
                }

                result[0] = hWnd;
                return false; // stop enumeration
            }, Pointer.NULL);

            if (useTitleSwap) {
                frame.setTitle(title);
            }

            return SafeOpt.ofNullable(result)
                    .map(r -> r[0]);
        } catch (Exception e) {
            return SafeOpt.error(e);
        }
    }

    private static final SafeOpt<Method> getPeerMethod = SafeOpt.ofLazy("com.sun.javafx.stage.WindowHelper")
            .map(Class::forName)
            .flatMapOpt(
                    cls -> ReflMethods.getMethods(cls)
                            .filter(m -> m.isStatic() && m.nameIs("getPeer") && m.getParameterCount() == 1).findAny())
            .map(method -> {
                method.setAccessible(true);
                return method.method();
            });

    private static final SafeOpt<Method> getRawHandle = SafeOpt.ofLazy("com.sun.javafx.tk.TKStage")
            .map(Class::forName)
            .flatMapOpt(
                    cls -> ReflMethods.getMethods(cls)
                            .filter(
                                    m -> !m.isStatic()
                                    && m.nameIs("getRawHandle")
                                    && m.hasNoParameters()
                                    && m.isReturnTypeExactly(Long.TYPE)).findAny())
            .map(method -> {
                method.setAccessible(true);
                return method.method();
            });

    /**
     * Using internal javaFX WindowHelper api. Using reflection to compile under
     * java8
     *
     * @param window
     * @return
     */
    public static SafeOpt<WinDef.HWND> getNativeHandleUsingInternals(Window window) {
        try {

            if (getPeerMethod.isPresent() && getRawHandle.isPresent()) {
                return SafeOpt.ofNullable(window)
                        .map(win -> {
                            return getPeerMethod.get().invoke(null, win); //WindowHelper.getPeer(win)
                        })
                        .map(tkstage -> {
                            return (Long) getRawHandle.get().invoke(tkstage);// tkstage.getRawHandle()
                        })
                        .map(handle -> new Pointer(handle))
                        .map(pointer -> new WinDef.HWND(pointer));
            } else {
                return INTERNALS_ERROR;
            }

        } catch (Exception e) {
            return SafeOpt.error(e);
        }
    }

//    private static SafeOpt<WinDef.HWND> getNativeHandle(Window window) {
//        return SafeOpt.ofNullable(window)
//                .map(win -> WindowHelper.getPeer(win))
//                .map(peer -> peer.getRawHandle())
//                .map(handle -> new Pointer(handle))
//                .map(pointer -> new WinDef.HWND(pointer));
//    }
    public static boolean isWindows() {
        return Java.getOSname().toLowerCase().contains("win");
    }

}
