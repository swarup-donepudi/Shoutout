package com.shoutout;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by swarup921 on 4/25/2015.
 */
public class AudioClip {
    private int clipId;
    private String clipTitle;
    private String clipTags;
    private String clipCategory;
    private String clipImageUrl;
    private String clipAudioUrl;
    private Bitmap clipImage;
    private byte[] audioClip;

    public AudioClip(){
        super();
    }

    public AudioClip(int clipId, String clipTitle, String clipTags, String clipCategory) {
        this.clipId = clipId;
        this.clipTitle = clipTitle;
        this.clipTags = clipTags;
        this.clipCategory = clipCategory;
    }

    public int getClipId() {
        return clipId;
    }

    public void setClipId(int clipId) {
        this.clipId = clipId;
    }

    public String getClipTitle() {
        return clipTitle;
    }

    public void setClipTitle(String clipTitle) {
        this.clipTitle = clipTitle;
    }

    public String getClipTags() {
        return clipTags;
    }

    public void setClipTags(String clipTags) {
        this.clipTags = clipTags;
    }

    public String getClipCategory() {
        return clipCategory;
    }

    public void setClipCategory(String clipCategory) {
        this.clipCategory = clipCategory;
    }

    public String getClipImageUrl() {
        return clipImageUrl;
    }

    public void setClipImageUrl(String clipImageUrl) {
        this.clipImageUrl = clipImageUrl;
    }

    public String getClipAudioUrl() {
        return clipAudioUrl;
    }

    public void setClipAudioUrl(String clipAudioUrl) {
        this.clipAudioUrl = clipAudioUrl;
    }

    public Bitmap getClipImage() {
        return clipImage;
    }

    public void setClipImage(Bitmap clipImage) {
        this.clipImage = clipImage;
    }

    public byte[] getAudioClip() {
        return audioClip;
    }

    public void setAudioClip(byte[] audioClip) {
        this.audioClip = audioClip;
    }
}
