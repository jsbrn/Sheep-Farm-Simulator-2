package com.bitbucket.computerology.main;
import com.jdotsoft.JarClassLoader;

public class Launcher {
    public static void main(String[] args) {
        JarClassLoader jcl = new JarClassLoader();
        try {
            jcl.invokeMain("com.bitbucket.computerology.main.SlickInitializer", args);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
