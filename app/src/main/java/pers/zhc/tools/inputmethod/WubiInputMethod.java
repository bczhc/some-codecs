package pers.zhc.tools.inputmethod;

import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.TextView;
import pers.zhc.tools.R;
import pers.zhc.tools.test.wubiinput.WubiInput;
import pers.zhc.tools.utils.sqlite.MySQLite3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WubiInputMethod extends InputMethodService {
    private final StringBuilder wubiCodeSB = new StringBuilder();
    private MySQLite3 wubiDictDB = null;
    private TextView candidateTV, wubiCodeTV;

    @Override
    public View onCreateInputView() {
        if (wubiDictDB == null) {
            wubiDictDB = WubiInput.getWubiDictDatabase(this);
        }
        View candidateView = View.inflate(this, R.layout.wubi_input_method_candidate_view, null);
        candidateTV = candidateView.findViewById(R.id.candidates);
        wubiCodeTV = candidateView.findViewById(R.id.code);
        setCandidatesView(candidateView);
        setCandidatesViewShown(true);
        return super.onCreateInputView();
    }

    private boolean checkAcceptedKeyCodeRange(int keyCode) {
        return (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z)
                | (keyCode == KeyEvent.KEYCODE_COMMA || keyCode == KeyEvent.KEYCODE_PERIOD)
                | keyCode == KeyEvent.KEYCODE_DEL
                | keyCode == KeyEvent.KEYCODE_SPACE;
    }

    private final List<String> candidates = new ArrayList<>();

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        InputConnection ic = getCurrentInputConnection();

        boolean accept = checkAcceptedKeyCodeRange(keyCode);
        if (accept) {
            if (wubiCodeSB.length() == 4
                    && keyCode != KeyEvent.KEYCODE_COMMA
                    && keyCode != KeyEvent.KEYCODE_PERIOD
                    && keyCode != KeyEvent.KEYCODE_DEL) {
                commitTheFirstCandidate(ic);
                clearWubiCodeSB();
                candidates.clear();
            }
            switch (keyCode) {
                case KeyEvent.KEYCODE_SPACE:
                    commitTheFirstCandidate(ic);
                    candidates.clear();
                    clearWubiCodeSB();
                    break;
                case KeyEvent.KEYCODE_COMMA:
                    commitTheFirstCandidate(ic);
                    candidates.clear();
                    clearWubiCodeSB();
                    ic.commitText("，", 0);
                    break;
                case KeyEvent.KEYCODE_PERIOD:
                    commitTheFirstCandidate(ic);
                    candidates.clear();
                    clearWubiCodeSB();
                    ic.commitText("。", 0);
                    break;
                case KeyEvent.KEYCODE_DEL:
                    if (wubiCodeSB.length() == 0) {
                        ic.deleteSurroundingText(1, 0);
                    } else wubiCodeSB.deleteCharAt(wubiCodeSB.length() - 1);
                    break;
                default:
                    wubiCodeSB.append(((char) event.getUnicodeChar()));
                    break;
            }
            refresh();
        }
        return accept;
    }

    private void commitTheFirstCandidate(InputConnection ic) {
        if (candidates.size() != 0)
            ic.commitText(candidates.get(0), 0);
    }

    private void clearWubiCodeSB() {
        wubiCodeSB.delete(0, wubiCodeSB.length());
    }

    private void refresh() {
        String wubiCodeStr = wubiCodeSB.toString();
        setCandidatesField(wubiCodeStr);
        String candidatesString = Arrays.toString(candidates.toArray());
        candidateTV.setText(candidatesString);
        wubiCodeTV.setText(wubiCodeStr);
    }

    private void setCandidatesField(String wubiCodeStr) {
        candidates.clear();
        wubiDictDB.exec("SELECT char FROM wubi_dict WHERE code IS '" + wubiCodeStr + "' ORDER BY num DESC", contents -> {
            String candidate = contents[0];
            candidates.add(candidate);
            return 0;
        });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return checkAcceptedKeyCodeRange(keyCode);
    }
}