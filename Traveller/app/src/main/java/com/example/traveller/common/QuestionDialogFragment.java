package com.example.traveller.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.example.traveller.R;
import com.example.traveller.ResolveDialogFragment;
import com.example.traveller.common.cloudanchor.FirebaseManager;
import com.google.common.base.Preconditions;

import java.util.ArrayList;

public class QuestionDialogFragment extends DialogFragment {

    public interface OkListener {
        /**
         * This method is called by the dialog box when its OK button is pressed.
         *
         * @param isCorrectTreasureAnswer boolean value, true if the answer was correct, false otherwise
         */
        void onOkPressed(boolean isCorrectTreasureAnswer);
    }

    private RadioButton rb1;
    private RadioButton rb2;
    private RadioButton rb3;
    private RadioButton rb4;
    private TextView textViewQuestion;
    private QuestionDialogFragment.OkListener okListener;

    public void setOkListener(QuestionDialogFragment.OkListener okListener) {
        this.okListener = okListener;
    }

    /**This Method is to be called when one wants to show this dialog
     * just call newInstance.show()*/
    public static QuestionDialogFragment newInstance(String question, String correctAnswer, String wrongAnswer1, String wrongAnswer2, String wrongAnswer3) {
        QuestionDialogFragment frag = new QuestionDialogFragment();
        Bundle args = new Bundle();
        args.putString("question", question);
        args.putString("answer", correctAnswer);
        args.putString("wrongAnswer1", wrongAnswer1);
        args.putString("wrongAnswer2", wrongAnswer2);
        args.putString("wrongAnswer3", wrongAnswer3);
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        FragmentActivity activity =
                Preconditions.checkNotNull(getActivity(), "The activity cannot be null.");
        AlertDialog.Builder builder=new AlertDialog.Builder(activity);
        LayoutInflater inflater=requireActivity().getLayoutInflater();

        //get the question and correct answer from bundle
        String question=getArguments().getString("question");
        String answer=getArguments().getString("answer");
        String wrongAnswer1=getArguments().getString("wrongAnswer1");
        String wrongAnswer2=getArguments().getString("wrongAnswer2");
        String wrongAnswer3=getArguments().getString("wrongAnswer3");

        View dialogView = activity.getLayoutInflater().inflate(R.layout.question_dialog, null);

        AlertDialog toReturn= builder.setView(dialogView)
                .setTitle(R.string.question_dialog_title)
                .setIcon(R.drawable.outline_quiz_black_48dp)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //get the text of the selected radio button
                        //compare if it's the correct answer
                        //and call the okListener
                        String toReturn="";
                        if(rb1.isChecked()) toReturn=rb1.getText().toString();
                        if(rb2.isChecked()) toReturn=rb2.getText().toString();
                        if(rb3.isChecked()) toReturn=rb3.getText().toString();
                        if(rb4.isChecked()) toReturn=rb4.getText().toString();

                        if(okListener!=null) {
                            if (toReturn.equals(answer))
                                okListener.onOkPressed(true);
                            else okListener.onOkPressed(false);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();

        rb1=dialogView.findViewById(R.id.radioButtonAnswer1);
        rb2=dialogView.findViewById(R.id.radioButtonAnswer2);
        rb3=dialogView.findViewById(R.id.radioButtonAnswer3);
        rb4=dialogView.findViewById(R.id.radioButtonAnswer4);
        textViewQuestion=dialogView.findViewById(R.id.textViewQuestion_question_dialog);

        textViewQuestion.setText(question);
        double num=Math.random();

        //some basic logic for making the place of the right answer random
        if(num>=0 && num<0.25){
            rb1.setText(answer);
            rb2.setText(wrongAnswer1);
            rb3.setText(wrongAnswer2);
            rb4.setText(wrongAnswer3);
        } else if(num>=0.25 && num<0.5){
            rb2.setText(answer);
            rb1.setText(wrongAnswer1);
            rb3.setText(wrongAnswer2);
            rb4.setText(wrongAnswer3);
        } else if(num>=0.5 && num<0.75){
            rb3.setText(answer);
            rb1.setText(wrongAnswer1);
            rb2.setText(wrongAnswer2);
            rb4.setText(wrongAnswer3);
        } else {
            rb4.setText(answer);
            rb1.setText(wrongAnswer1);
            rb3.setText(wrongAnswer2);
            rb2.setText(wrongAnswer3);
        }

        return toReturn;
    }
}
