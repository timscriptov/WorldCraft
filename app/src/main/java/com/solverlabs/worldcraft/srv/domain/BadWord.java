package com.solverlabs.worldcraft.srv.domain;

import androidx.annotation.NonNull;

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

    public String getReplacement() {
        return this.replacement;
    }

    public String getWord() {
        return this.word;
    }

    public void setId(long j) {
        this.id = j;
    }

    public void setReplacement(String str) {
        this.replacement = str;
    }

    public void setWord(String str) {
        this.word = str;
    }

    @NonNull
    public String toString() {
        return "BadWord{}" + this.id + "," + this.word;
    }
}
