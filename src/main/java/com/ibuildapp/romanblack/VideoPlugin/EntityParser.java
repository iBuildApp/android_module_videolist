/****************************************************************************
*                                                                           *
*  Copyright (C) 2014-2015 iBuildApp, Inc. ( http://ibuildapp.com )         *
*                                                                           *
*  This file is part of iBuildApp.                                          *
*                                                                           *
*  This Source Code Form is subject to the terms of the iBuildApp License.  *
*  You can obtain one at http://ibuildapp.com/license/                      *
*                                                                           *
****************************************************************************/
package com.ibuildapp.romanblack.VideoPlugin;

import android.graphics.Color;
import android.sax.Element;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import org.xml.sax.Attributes;

import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Log;
import android.util.Xml;

/**
 * This class using for parsing module xml data.
 */
public class EntityParser {

    private ArrayList<VideoItem> items = new ArrayList<VideoItem>();
    private int color1 = Color.parseColor("#4d4948");// background
    private int color2 = Color.parseColor("#fff58d");// category header
    private int color3 = Color.parseColor("#fff7a2");// text header
    private int color4 = Color.parseColor("#ffffff");// text
    private int color5 = Color.parseColor("#bbbbbb");// date
    private String xml = "";
    private String title = "";
    private VideoItem item = null;
    private String appId = "0";
    private String moduleId = "0";
    private String appName = "";
    private String sharingOn = "off";
    private String commentsOn = "off";

    /**
     * Constructs new EntityParser instance.
     * @param xml - module xml data to parse
     */
    EntityParser(String xml) {
        this.xml = xml;
    }

    /**
     * Returns the parsed module title.
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the parsed video items.
     * @return the parsed video items
     */
    public ArrayList<VideoItem> getItems() {
        return items;
    }

    /**
     * Parses module data that was set in constructor.
     */
    public void parse() {
        RootElement root = new RootElement("data");
        android.sax.Element title1 = root.getChild("title");

        android.sax.Element appIdElement = root.getChild("app_id");
        appIdElement.setEndTextElementListener(new EndTextElementListener() {
            public void end(String arg0) {
                appId = arg0.trim();
            }
        });

        android.sax.Element moduleIdElement = root.getChild("module_id");
        moduleIdElement.setEndTextElementListener(new EndTextElementListener() {
            public void end(String arg0) {
                moduleId = arg0.trim();
            }
        });

        android.sax.Element appNameElement = root.getChild("app_name");
        appNameElement.setEndTextElementListener(new EndTextElementListener() {
            public void end(String arg0) {
                appName = arg0.trim();
            }
        });

        Element colorSchemeElement = root.getChild("colorskin");

        Element color1Element = colorSchemeElement.getChild("color1");
        color1Element.setEndTextElementListener(new EndTextElementListener() {
            public void end(String arg0) {
                color1 = Color.parseColor(arg0.trim());
            }
        });

        Element color2Element = colorSchemeElement.getChild("color2");
        color2Element.setEndTextElementListener(new EndTextElementListener() {
            public void end(String arg0) {
                color2 = Color.parseColor(arg0.trim());
            }
        });

        Element color3Element = colorSchemeElement.getChild("color3");
        color3Element.setEndTextElementListener(new EndTextElementListener() {
            public void end(String arg0) {
                color3 = Color.parseColor(arg0.trim());
            }
        });

        Element color4Element = colorSchemeElement.getChild("color4");
        color4Element.setEndTextElementListener(new EndTextElementListener() {
            public void end(String arg0) {
                color4 = Color.parseColor(arg0.trim());
            }
        });

        Element color5Element = colorSchemeElement.getChild("color5");
        color5Element.setEndTextElementListener(new EndTextElementListener() {
            public void end(String arg0) {
                color5 = Color.parseColor(arg0.trim());
            }
        });

        android.sax.Element allowSharingElement = root.getChild("allowsharing");
        allowSharingElement.setEndTextElementListener(new EndTextElementListener() {
            public void end(String arg0) {
                sharingOn = arg0.trim();
            }
        });

        android.sax.Element allowCommentsElement = root.getChild("allowcomments");
        allowCommentsElement.setEndTextElementListener(new EndTextElementListener() {
            public void end(String arg0) {
                commentsOn = arg0.trim();
            }
        });

        android.sax.Element media = root.getChild("video");

        root.setEndElementListener(new EndElementListener() {
            @Override
            public void end() {
            }
        });

        title1.setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                EntityParser.this.title = body.trim();
            }
        });

        media.setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attributes) {
                item = new VideoItem();
            }
        });

        media.setEndElementListener(new EndElementListener() {
            @Override
            public void end() {
                items.add(item);
                item = null;
            }
        });

        media.getChild("title").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (item != null) {
                    item.setTitle(body.trim());
                }
            }
        });

        media.getChild("url").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (item != null) {
                    item.setUrl(body.trim());
                }
            }
        });

        media.getChild("cover").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (item != null) {
                    item.setCoverUrl(body.trim());
                }
            }
        });

        media.getChild("description").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (item != null) {
                    item.setDescription(body.trim());
                }
            }
        });

        media.getChild("id").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (item != null) {
                    item.setId(Long.parseLong(body.trim()));
                }
            }
        });

        try {
            Xml.parse(new ByteArrayInputStream(xml.getBytes()), Xml.Encoding.UTF_8, root.getContentHandler());
        } catch (Exception e) {
            Log.d("", "");
        }
    }

    /**
     * Returns the application ID.
     * @return the application ID
     */
    public String getAppId() {
        return appId;
    }

    /**
     * Returns the module ID.
     * @return the module ID
     */
    public String getModuleId() {
        return moduleId;
    }

    /**
     * Returns the application name.
     * @return the application name
     */
    public String getAppName() {
        return appName;
    }

    /**
     * Returns whether to allow sharing.
     * @return "on" if sharing is allowed, "off" if is not allowed
     */
    public String getSharingOn() {
        return sharingOn;
    }

    /**
     * Returns whether to allow comments.
     * @return "on" if comments is allowed, "off" if is not allowed
     */
    public String getCommentsOn() {
        return commentsOn;
    }

    /**
     * @return parsed color 1 of color scheme
     */
    public int getColor1() {
        return color1;
    }

    /**
     * @return parsed color 2 of color scheme
     */
    public int getColor2() {
        return color2;
    }

    /**
     * @return parsed color 3 of color scheme
     */
    public int getColor3() {
        return color3;
    }

    /**
     * @return parsed color 4 of color scheme
     */
    public int getColor4() {
        return color4;
    }

    /**
     * @return parsed color 5 of color scheme
     */
    public int getColor5() {
        return color5;
    }
}
