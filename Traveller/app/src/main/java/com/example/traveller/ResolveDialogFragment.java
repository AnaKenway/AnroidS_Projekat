/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.traveller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import com.google.common.base.Preconditions;

/** A DialogFragment for the Resolve Dialog Box. */
public class ResolveDialogFragment extends DialogFragment {

  interface OkListener {
    /**
     * This method is called by the dialog box when its OK button is pressed.
     *
     * @param treasureName the long value from the dialog box
     */
    void onOkPressed(String treasureName);
  }

  private EditText treasureNameField;
  private OkListener okListener;

  public void setOkListener(OkListener okListener) {
    this.okListener = okListener;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    FragmentActivity activity =
        Preconditions.checkNotNull(getActivity(), "The activity cannot be null.");
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);

    // Passing null as the root is fine, because the view is for a dialog.
    View dialogView = activity.getLayoutInflater().inflate(R.layout.resolve_dialog, null);
    treasureNameField = dialogView.findViewById(R.id.treasure_name_input);
    builder
        .setView(dialogView)
        .setTitle(R.string.resolve_dialog_title)
        .setPositiveButton(
            R.string.resolve_dialog_ok,
            (dialog, which) -> {
              Editable treasureNameText = treasureNameField.getText();
              if (okListener != null && treasureNameText != null && treasureNameText.length() > 0) {
                //Long longVal = Long.valueOf(treasureNameText.toString());
                okListener.onOkPressed(treasureNameText.toString());
              }
            })
        .setNegativeButton(R.string.cancel, (dialog, which) -> {});
    return builder.create();
  }
}
