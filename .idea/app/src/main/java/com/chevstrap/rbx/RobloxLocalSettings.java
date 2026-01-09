package com.chevstrap.rbx;

import com.chevstrap.rbx.AppDirectories.RobloxClientData;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Locale;

public class RobloxLocalSettings {
    private static final RobloxClientData ROBLOX_DATA = new RobloxClientData();
    private static final String SETTINGS_PATH = new File(
            ROBLOX_DATA.getExecutablePath(),
            "GlobalBasicSettings_13.xml"
    ).getAbsolutePath();

    private static final Object LOCK = new Object();

    public static void setASetting(String name, Object value) {
        if (value == null) return;

        if (value instanceof Boolean) {
            setPropertyValue("bool", name, Boolean.toString((Boolean) value).toLowerCase(Locale.ROOT));
        }
        else if (value instanceof Integer) {
            setPropertyValue("int", name, Integer.toString((Integer) value));
        }
        else if (value instanceof Float) {
            setPropertyValue("float", name, String.format(Locale.ROOT, "%f", (Float) value));
        }
        else if (value instanceof String) {
            setPropertyValue("string", name, (String) value);
        }
        else if (value instanceof float[]) {
            float[] arr = (float[]) value;
            if (arr.length != 2) {
                App.getLogger().writeLine("RobloxLocalSettings", "Invalid Vector2 length for " + name);
                return;
            }

            synchronized (LOCK) {
                File file = new File(SETTINGS_PATH);
                if (!file.exists()) {
                    App.getLogger().writeLine("RobloxLocalSettings", "Settings file not found: " + SETTINGS_PATH);
                    return;
                }

                try {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(file);
                    doc.getDocumentElement().normalize();

                    Element properties = findProperties(doc);
                    if (properties == null) {
                        App.getLogger().writeLine("RobloxLocalSettings", "Properties tag not found");
                        return;
                    }

                    NodeList vecNodes = properties.getElementsByTagName("Vector2");
                    Element target = null;
                    for (int i = 0; i < vecNodes.getLength(); i++) {
                        Element el = (Element) vecNodes.item(i);
                        if (name.equals(el.getAttribute("name"))) {
                            target = el;
                            break;
                        }
                    }

                    float x = arr[0];
                    float y = arr[1];

                    if (target != null) {
                        replaceText(target, "X", String.format(Locale.ROOT, "%f", x));
                        replaceText(target, "Y", String.format(Locale.ROOT, "%f", y));
                    } else {
                        Element newVec = doc.createElement("Vector2");
                        newVec.setAttribute("name", name);

                        Element xEl = doc.createElement("X");
                        xEl.setTextContent(String.format(Locale.ROOT, "%f", x));
                        Element yEl = doc.createElement("Y");
                        yEl.setTextContent(String.format(Locale.ROOT, "%f", y));

                        newVec.appendChild(xEl);
                        newVec.appendChild(yEl);
                        properties.appendChild(newVec);
                    }

                    saveDocument(doc, file);
                    App.getLogger().writeLine("RobloxLocalSettings", "Updated Vector2 " + name + " = (" + x + ", " + y + ")");
                } catch (Exception e) {
                    App.getLogger().writeException("RobloxLocalSettings::set(Vector2)", e);
                }
            }
        }
        else {
            App.getLogger().writeLine("RobloxLocalSettings", "Unsupported type for " + name + ": " + value.getClass().getSimpleName());
        }
    }

    private static void setPropertyValue(String type, String name, String value) {
        synchronized (LOCK) {
            File file = new File(SETTINGS_PATH);
            if (!file.exists()) {
                App.getLogger().writeLine("RobloxLocalSettings", "Settings file not found: " + SETTINGS_PATH);
                return;
            }

            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(file);
                doc.getDocumentElement().normalize();

                Element properties = findProperties(doc);
                if (properties == null) {
                    App.getLogger().writeLine("RobloxLocalSettings", "Properties tag not found");
                    return;
                }

                NodeList nodes = properties.getElementsByTagName(type);
                Element target = null;
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element el = (Element) nodes.item(i);
                    if (name.equals(el.getAttribute("name"))) {
                        target = el;
                        break;
                    }
                }

                if (target != null) {
                    target.setTextContent(value);
                } else {
                    Element newEl = doc.createElement(type);
                    newEl.setAttribute("name", name);
                    newEl.setTextContent(value);
                    properties.appendChild(newEl);
                }

                saveDocument(doc, file);
                App.getLogger().writeLine("RobloxLocalSettings", "Updated " + type + " " + name + " = " + value);
            } catch (Exception e) {
                App.getLogger().writeException("RobloxLocalSettings::setPropertyValue", e);
            }
        }
    }
    private static Element findProperties(Document doc) {
        NodeList itemNodes = doc.getElementsByTagName("Item");
        for (int i = 0; i < itemNodes.getLength(); i++) {
            Element item = (Element) itemNodes.item(i);
            if ("UserGameSettings".equals(item.getAttribute("class"))) {
                NodeList propsList = item.getElementsByTagName("Properties");
                if (propsList.getLength() > 0)
                    return (Element) propsList.item(0);
            }
        }
        return null;
    }

    private static void replaceText(Element parent, String tagName, String newValue) {
        NodeList list = parent.getElementsByTagName(tagName);
        if (list.getLength() > 0) {
            list.item(0).setTextContent(newValue);
        } else {
            Element el = parent.getOwnerDocument().createElement(tagName);
            el.setTextContent(newValue);
            parent.appendChild(el);
        }
    }

    private static void saveDocument(Document doc, File file) throws TransformerException, IOException {
        App.getLogger().writeLine("RobloxLocalSettings", "Saving XML to: " + file.getAbsolutePath());

        if (doc == null) {
            App.getLogger().writeLine("RobloxLocalSettings", "Document is null — nothing to save.");
            return;
        }

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        try (FileOutputStream out = new FileOutputStream(file)) {
            transformer.transform(new DOMSource(doc), new StreamResult(out));
            out.getFD().sync();
            App.getLogger().writeLine("RobloxLocalSettings", "File saved successfully (" + file.length() + " bytes)");
        } catch (IOException e) {
            App.getLogger().writeException("RobloxLocalSettings::saveDocument (IO)", e);
            throw e;
        } catch (TransformerException e) {
            App.getLogger().writeException("RobloxLocalSettings::saveDocument (Transformer)", e);
            throw e;
        } catch (Exception e) {
            App.getLogger().writeException("RobloxLocalSettings::saveDocument (Unexpected)", e);
            throw e;
        }
    }

}
