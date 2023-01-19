package com.solverlabs.worldcraft.srv.domain;


public class BadWord {
    private long id;
    private String replacement;
    private String word;

    public BadWord(long j, String str, String str2) {
        this.id = j;
        this.word = str;
        this.replacement = str2;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long j) {
        this.id = j;
    }

    public String getReplacement() {
        return this.replacement;
    }

    public void setReplacement(String str) {
        this.replacement = str;
    }

    public String getWord() {
        return this.word;
    }

    public void setWord(String str) {
        this.word = str;
    }

    public String toString() {
        return new StringBuffer("BadWord{}").append(this.id).append(",").append(this.word).toString();
    }
}
