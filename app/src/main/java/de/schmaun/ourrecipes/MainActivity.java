package de.schmaun.ourrecipes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import de.schmaun.ourrecipes.Main.LabelsListFragment;
import de.schmaun.ourrecipes.Main.RecipesListByLabelFragment;
import de.schmaun.ourrecipes.Main.RecipesListFavoritesFragment;
import de.schmaun.ourrecipes.Model.Label;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LabelsListFragment.LabelListInteractionListener {

    public static final String TAG = "MainActivity";
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context context = this;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EditRecipeActivity.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            showLabelsListFragmentOnCreate();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null && account.getPhotoUrl() != null) {
            final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            final View header = navigationView.getHeaderView(0);
            final ImageView navigationDrawerImage = (ImageView) header.findViewById(R.id.nav_header_image);

            Glide.with(this).load(account.getPhotoUrl()).into(navigationDrawerImage);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            toggle.setDrawerIndicatorEnabled(true);
            toggle.setHomeAsUpIndicator(null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.nav_start:
                showLabelsListFragment(true);
                break;
            case R.id.nav_favorites:
                showRecipesListFavoritesFragment();
                break;
            case R.id.nav_settings:
                startSettings();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onLabelsListLabelClick(Label label) {
        showRecipesListByLabelFragment(label);
    }

    private void showLabelsListFragmentOnCreate() {
        navigationView.getMenu().getItem(0).setChecked(true);
        this.showLabelsListFragment(false);
    }

    private void showLabelsListFragment(boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_main, LabelsListFragment.newInstance());

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }

    private void showRecipesListByLabelFragment(Label label) {
        toggle.setHomeAsUpIndicator(R.drawable.back);
        toggle.setDrawerIndicatorEnabled(false);
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        setTitle(label.getName());
        RecipesListByLabelFragment fragment = RecipesListByLabelFragment.newInstance(label);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_main, fragment, "recipes")
                .addToBackStack(null)
                .commit();
    }

    private void showRecipesListFavoritesFragment() {
        setTitle(getString(R.string.favorites));
        RecipesListFavoritesFragment fragment = RecipesListFavoritesFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_main, fragment, "recipes")
                .addToBackStack(null)
                .commit();
    }
}
