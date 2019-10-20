package io.github.cottonmc.cotton_scripting.impl.js;

import jdk.nashorn.api.scripting.ClassFilter;

public class JavascriptClassFilter implements ClassFilter {
    @Override
    public boolean exposeToScripts(String s) {
        return false;
    }
}
