/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package views

import html4k.InputType
import kotlin.browser.document
import html4k.js.*
import html4k.dom.*
import jquery.jq
import jquery.ui.button
import jquery.ui.dialog
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.Event

/**
 * Created by Semyon.Atamas on 5/18/2015.
 */


class InputDialogView(val title: String, val inputText: String, val buttonText: String) {
    companion object {
        var dialog: HTMLDivElement = document.create.div {
            id = "input-dialog"
        }

        var text = dialog.append.span {
            id = "input-dialog-text"
        }

        var input = dialog.append.input {
            id = "input-dialog-input"
            type = InputType.text
        }

        var message = dialog.append.div {
            classes = setOf("input-dialog-error-message");
        }
    }


    init {
        jq(dialog).dialog(json(
                "resizable" to false,
                "modal" to true,
                "width" to 380,
                "autoOpen" to false,
                "open" to {
                    input.select()
                }
        ))

        jq(dialog).keydown({ event ->
            if (event.keyCode == 13) {
                /*enter*/
                jq(dialog).parent()
                        .find("button:eq(1)").trigger("click");
            } else if (event.keyCode == 27) {
                /*escape*/
                jq(dialog).dialog("close");
            }
            event.stopPropagation();
        });
    }

    fun open(callback: (String) -> Unit, defaultValue: String) {
        input.focus()
        validationResult(ValidationResult(true));
        input.oninput = {
            validationResult(validate(input.value));
        };
        var verifiedDefaultValue = defaultValue;
        var i = 1;
        while (!validate(verifiedDefaultValue).valid) {
            verifiedDefaultValue = defaultValue + i;
            ++i;
        }
        input.value = verifiedDefaultValue;
        input.select();
        jq(dialog).dialog("option", "title", title);
        text.innerHTML = inputText;
        jq(dialog).dialog("option", "buttons", arrayOf(
                json(
                        "text" to buttonText,
                        "click" to { event: Event ->
                            var validationResult = validate(input.value);
                            if (validationResult.valid) {
                                callback(input.value);
                                jq(dialog).dialog("close");
                            } else {
                                validationResult(validationResult);
                            }
                            event.stopPropagation();
                        }
                ),
                json(
                        "text" to "Cancel",
                        "click" to { event: Event ->
                            jq(dialog).dialog("close");
                            event.stopPropagation();
                        }
                )
        )
        );
        jq(dialog).dialog("open");
    }

    var validate: (String) -> ValidationResult = { input: String ->
        ValidationResult(true)
    }

    fun validationResult(result: ValidationResult) {
        input.style.outlineColor = if (result.valid) "" else "red";
        jq(dialog).parent().find("button:eq(1)").button("option", "disabled", !result.valid);
        if (!result.valid) {
            message.innerHTML = result.message;
        } else {
            message.innerHTML = "";
        }
        input.focus();
    }

}