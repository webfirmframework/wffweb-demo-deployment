package com.webfirmframework.ui.page.component;

import com.webfirmframework.ui.page.common.GlobalSTC;
import com.webfirmframework.ui.page.css.Bootstrap5CssClass;
import com.webfirmframework.wffweb.tag.html.Br;
import com.webfirmframework.wffweb.tag.html.attribute.Name;
import com.webfirmframework.wffweb.tag.html.attribute.Role;
import com.webfirmframework.wffweb.tag.html.attribute.Type;
import com.webfirmframework.wffweb.tag.html.attribute.event.form.OnSubmit;
import com.webfirmframework.wffweb.tag.html.attribute.global.Id;
import com.webfirmframework.wffweb.tag.html.formsandinputs.Button;
import com.webfirmframework.wffweb.tag.html.formsandinputs.Form;
import com.webfirmframework.wffweb.tag.html.formsandinputs.Input;
import com.webfirmframework.wffweb.tag.html.formsandinputs.Label;
import com.webfirmframework.wffweb.tag.html.stylesandsemantics.Div;
import com.webfirmframework.wffweb.tag.htmlwff.TagContent;
import com.webfirmframework.wffweb.wffbm.data.WffBMByteArray;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SampleFilesUploadWithoutActionUrlComponent extends Div {

    private record FileHolder(String fileName, String fileType, byte[] bytes) {
    }

    public SampleFilesUploadWithoutActionUrlComponent() {
        super(null, Bootstrap5CssClass.CONTAINER.getAttribute());
        GlobalSTC.LOGGER_STC.setContent(
                ZonedDateTime.now(Clock.systemUTC()).format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")) +
                        ":~$ created new " + getClass().getSimpleName());

        develop();
    }

    private void develop() {

        new Br(this);
        new Br(this);
        Div displayMsgDiv = new Div(this);

        new Form(this, new Id("inputFieldsForm"), new OnSubmit(true, """
                loadingIcon.hidden = false;
                formSubmitBtn.setAttribute('disabled', 'disabled');
                filesForm.requestSubmit();
                return true;
                """.stripIndent(),
                event -> {
                    String fullName = event.data().getValueAsString("fullName");
                    GlobalSTC.LOGGER_STC.setContent("event.data().getValue(fullName) = " + fullName);

                    Div alert = new Div(null,
                            Bootstrap5CssClass.ALERT_SUCCESS.getClassAttribute(),
                            new Role(Role.ALERT)).give(TagContent::text, "form input values received, fullName = " + fullName);
                    displayMsgDiv.appendChild(alert);

                    return null;
                }, "return {fullName: fullName.value};",
                """
                        inputFieldsForm.reset();
                        filesForm.reset();
                        formSubmitBtn.removeAttribute('disabled');
                        loadingIcon.hidden = true;
                        """)).give(form -> {

            new Div(form, Bootstrap5CssClass.FORM_GROUP.getAttribute()).give(dv -> {
                new Label(dv).give(TagContent::text, "Full Name");
                new Input(dv, new Type(Type.TEXT), new Name("fullName"), Bootstrap5CssClass.FORM_CONTROL.getAttribute());
            });


            final ByteArrayOutputStream fileBAOS = new ByteArrayOutputStream();
            final List<FileHolder> uploadedFiles = new ArrayList<>(2);
            new Form(form, new Id("filesForm"), new OnSubmit(true,
                    """
                            const file1 = inputFile1.files[0];
                            const file2 = inputFile2.files[0];
                            
                            const CHUNK_SIZE = 512 * 1024;
                            
                            const readerForFile1 = new FileReader();
                            readerForFile1.onload = function(fre) {
                                const int8Array = new Int8Array(fre.target.result);
                                for (let i = 0; i < int8Array.length; i += CHUNK_SIZE) {
                                    source.fileChunk = int8Array.slice(i, i + CHUNK_SIZE);
                                    source.fileChunkLast = (i + CHUNK_SIZE) >= int8Array.length;
                                    if (source.fileChunkLast) {
                                        source.fileName = file1.name;
                                        source.fileType = file1.type;
                                    }
                                    action.perform();
                                }
                            };
                            if (file1) {
                                readerForFile1.readAsArrayBuffer(file1);
                            }
                            
                            const readerForFile2 = new FileReader();
                            readerForFile2.onload = function(fre) {
                                const int8Array = new Int8Array(fre.target.result);
                                for (let i = 0; i < int8Array.length; i += CHUNK_SIZE) {
                                    const chunk = int8Array.slice(i, i + CHUNK_SIZE);
                                    source.fileChunk = chunk;
                                    source.fileChunkLast = (i + CHUNK_SIZE) >= int8Array.length;
                                    if (source.fileChunkLast) {
                                        source.fileName = file2.name;
                                        source.fileType = file2.type;
                                    }
                                    action.perform();
                                }
                            };
                            if (file2) {
                                readerForFile2.readAsArrayBuffer(file2);
                            }
                            """, event -> {

                //Note: all event attributes like onsubmit, onclick etc.. events will read from/write to the main memory.

                final WffBMByteArray fileChunk = event.data().getValueAsWffBMByteArray("fileChunk");
                final boolean fileChunkLast = event.data().getValueAsBoolean("fileChunkLast");
                try {
                    fileChunk.writeTo(fileBAOS);
                    if (fileChunkLast) {
                        final String fileName = event.data().getValueAsString("fileName");
                        final String fileType = event.data().getValueAsString("fileType");
                        final FileHolder file = new FileHolder(fileName, fileType, fileBAOS.toByteArray());
                        fileBAOS.reset();
                        uploadedFiles.add(file);
                        Div alert = new Div(null,
                                Bootstrap5CssClass.ALERT_SUCCESS.getClassAttribute(),
                                new Role(Role.ALERT)).give(TagContent::text, fileName + " file received");
                        displayMsgDiv.appendChild(alert);

                        GlobalSTC.LOGGER_STC.setContent(fileName + " received, mime type = " + fileType);
                        if (uploadedFiles.size() == 2) {
                            GlobalSTC.LOGGER_STC.setContent("received 2 files");
                        }
                        if ("text/plain".equals(fileType)) {
                            GlobalSTC.LOGGER_STC.setContent("The uploaded text file data:\n" + new String(file.bytes, StandardCharsets.UTF_8));
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                return null;
            }, """
                    const filePart = {fileChunk: source.fileChunk, fileChunkLast: source.fileChunkLast};
                    if (source.fileChunkLast) {
                        filePart.fileName = source.fileName;
                        filePart.fileType = source.fileType;
                    }
                    return filePart;""", null)).give(filesForm -> {
                new Div(filesForm, Bootstrap5CssClass.FORM_GROUP.getAttribute()).give(dv -> {
                    new Label(dv).give(TagContent::text, "Input File 1");
                    new Input(dv, new Type(Type.FILE), new Name("inputFile1"), Bootstrap5CssClass.FORM_CONTROL.getAttribute());
                });

                new Div(filesForm, Bootstrap5CssClass.FORM_GROUP.getAttribute()).give(dv -> {
                    new Label(dv).give(TagContent::text, "Input File 2");
                    new Input(dv, new Type(Type.FILE), new Name("inputFile2"), Bootstrap5CssClass.FORM_CONTROL.getAttribute());
                });
            });

            new Button(form, new Type(Type.SUBMIT), new Id("formSubmitBtn"), Bootstrap5CssClass.BTN_PRIMARY.getAttribute()).give(TagContent::text, "Upload file");
        });
    }
}
