package com.daniribalbert.letsplayfootball.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.content.res.AppCompatResources;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.daniribalbert.letsplayfootball.R;
import com.daniribalbert.letsplayfootball.utils.LogUtils;
import com.daniribalbert.letsplayfootball.utils.ToastUtils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity implements OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_GOOGLE_SIGN_IN = 2;
    private static final int RC_FACEBOOK_SIGN_IN = CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode();
    private static final int RC_TWITTER_SIGN_IN = Twitter.getInstance().getTwitterAuthConfig().getRequestCode();

    private static final List<String> FACEBOOK_PERMISSIONS = Arrays.asList("email", "public_profile");

    // UI references.
    @BindView(R.id.email)
    AutoCompleteTextView mEmailView;
    @BindView(R.id.password)
    EditText mPasswordView;

    @BindView(R.id.login_progress)
    View mProgressView;
    @BindView(R.id.login_form)
    View mLoginFormView;

    @BindView(R.id.email_sign_in_button)
    Button mEmailSignInButton;

    @BindView(R.id.google_sign_in_button)
    Button mGoogleSignInButton;

    @BindView(R.id.facebook_login_button)
    Button mFacebookLoginButton;

    @BindView(R.id.twitter_login_button)
    TwitterLoginButton mTwitterLoginButton;

    /**
     * Google API Client used to login with Google account.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Callback Manager used to login via Facebook SDK.
     */
    private CallbackManager mFacebookCallbackManager;
    private final OnCompleteListener<AuthResult> ON_COMPLETE_LISTENER = new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            showProgress(false);
            if (task.isSuccessful()) {
                LogUtils.d("signInWithCredential:success");
                loginCompleted();
            } else {
                LogUtils.w("signInWithCredential:failure", task.getException());
                ToastUtils.show(R.string.auth_failed, Toast.LENGTH_SHORT);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        showProgress(true);

        // Check for authenticated user.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            LogUtils.i("User is logged in!");
            loginCompleted();
        }

        initGoogleLogin();
        initFacebookLogin();
        initTwitterLogin();

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mEmailSignInButton.setOnClickListener(this);

        showProgress(false);
    }

    private void initGoogleLogin() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mGoogleSignInButton.setOnClickListener(this);
    }

    private void initFacebookLogin() {
        // Set Button Attrs
        final Resources res = getResources();

        mFacebookLoginButton.setText(getSignInText(getString(R.string.facebook)));
        // set vector drawables on the button
        mFacebookLoginButton.setCompoundDrawablesWithIntrinsicBounds(
                AppCompatResources.getDrawable(this, R.drawable.com_facebook_button_login_logo),
                null,
                null,
                null);
        mFacebookLoginButton.setCompoundDrawablePadding(
                res.getDimensionPixelSize(com.twitter.sdk.android.core.R.dimen.tw__login_btn_drawable_padding));
        mFacebookLoginButton.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                res.getDimensionPixelSize(com.twitter.sdk.android.core.R.dimen.tw__login_btn_text_size));
        mFacebookLoginButton.setPadding(res.getDimensionPixelSize(com.twitter.sdk.android.core.R.dimen.tw__login_btn_left_padding), 0,
                res.getDimensionPixelSize(com.twitter.sdk.android.core.R.dimen.tw__login_btn_right_padding), 0);


        mFacebookCallbackManager = CallbackManager.Factory.create();
        mFacebookLoginButton.setOnClickListener(this);

        LoginManager.getInstance().registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                LogUtils.i("Facebook Login: SUCCESS!");
                showProgress(true);
                final AccessToken accessToken = loginResult.getAccessToken();
                final String token = accessToken.getToken();
                final AuthCredential credential = FacebookAuthProvider.getCredential(token);
                loginResult.getAccessToken();
                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(LoginActivity.this, ON_COMPLETE_LISTENER);
            }

            @Override
            public void onCancel() {
                LogUtils.d("Facebook Login: Cancelled!");
            }

            @Override
            public void onError(FacebookException exception) {
                LogUtils.w("Facebook Login: ERROR!", exception);
                ToastUtils.show(R.string.auth_failed, Toast.LENGTH_SHORT);
            }
        });
    }

    private void initTwitterLogin() {
        mTwitterLoginButton.setText(getSignInText(getString(R.string.twitter)));
        mTwitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                LogUtils.d("twitterLogin:success" + result);
                showProgress(true);
                TwitterSession twitterSession = result.data;
                AuthCredential credential = TwitterAuthProvider.getCredential(
                        twitterSession.getAuthToken().token,
                        twitterSession.getAuthToken().secret);
                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(LoginActivity.this, ON_COMPLETE_LISTENER);
            }

            @Override
            public void failure(TwitterException exception) {
                LogUtils.w("twitterLogin:failure", exception);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        LogUtils.d("ON ACTIVITY RESULT! code" + requestCode + " result " + resultCode);
        switch (requestCode) {
            case RC_GOOGLE_SIGN_IN:
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleGoogleSignInResult(result);
                break;
        }
        if (requestCode == RC_FACEBOOK_SIGN_IN) {
            mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
            LogUtils.i("PEGOU FACEBOOK CARALHO!");
        } else if (requestCode == RC_TWITTER_SIGN_IN) {
            mTwitterLoginButton.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        LogUtils.d("handleGoogleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            showProgress(true);
            GoogleSignInAccount acct = result.getSignInAccount();
            AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

            mAuth.signInWithCredential(credential).addOnCompleteListener(this, ON_COMPLETE_LISTENER);
        } else {
            // Signed out, show unauthenticated UI.
            ToastUtils.show(R.string.auth_failed, Toast.LENGTH_SHORT);
        }
    }

    private void loginCompleted() {
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            signIn(email, password);
        }
    }

    private void signIn(final String email, final String password) {
        showProgress(true);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                showProgress(false);
                if (task.isSuccessful()) {
                    LogUtils.i("Login successful!");
                    loginCompleted();
                } else {
                    LogUtils.d("Login Failed. Should create new account?");
                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle(R.string.dialog_login_failed)
                            .setMessage(getString(R.string.error_sign_in_try_register))
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    signUp(email, password);
                                }
                            })
                            .setNegativeButton(R.string.back, null)
                            .show();
                }
            }
        });
    }

    private void signUp(final String email, final String password) {
        showProgress(true);
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                showProgress(false);
                if (task.isSuccessful()) {
                    LogUtils.i("New account created successfully!");
                    loginCompleted();
                } else {
                    LogUtils.w("Could not create a new account!");
                    ToastUtils.show(R.string.auth_failed, Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private boolean isEmailValid(String email) {
        return !TextUtils.isEmpty(email)
                && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.email_sign_in_button:
                attemptLogin();
                break;
            case R.id.google_sign_in_button:
                signInWithGoogle();
                break;
            case R.id.facebook_login_button:
                LoginManager.getInstance().logInWithReadPermissions(this, FACEBOOK_PERMISSIONS);
                break;
            case R.id.twitter_login_button:
                // Handled by Twitter Kit
                break;
        }
    }

    private void signInWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        LogUtils.d("onConnectionFailed:" + connectionResult);
    }

    private String getSignInText(String signInHere) {
        String signInGoogleTxt = getString(R.string.common_signin_button_text_long);
        return signInGoogleTxt.replace("Google", signInHere);
    }
}

