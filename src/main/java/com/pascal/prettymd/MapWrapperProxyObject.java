package com.pascal.prettymd;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.HashMap;
import java.util.Map;

public class MapWrapperProxyObject implements ProxyObject {

    Map<String, Value> members = new HashMap<>();

    public int getMemberAsInt(String key) {
        return members.get(key).asInt();
    }

    @Override
    public Object getMember(String key) {
        return members.get(key);
    }

    @Override
    public Object getMemberKeys() {
        return members.keySet().toArray();
    }

    @Override
    public boolean hasMember(String key) {
        return members.containsKey(key);
    }

    @Override
    public void putMember(String key, Value value) {
        members.put(key, value);
    }

}