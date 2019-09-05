package com.evasler.digipediamasteredition;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

@Dao
public interface MyDao {

    @Insert
    void addAttribute(Attribute attribute);
    @Query("SELECT * FROM Attribute")
    List<Attribute> getAllAttributes();

    @Insert
    void addFamily(Family family);
    @Query("SELECT * FROM Family")
    List<Family> getAllFamilies();

    @Insert
    void addLevel(Level level);
    @Query("SELECT * FROM Level")
    List<Level> getAllLevels();

    @Insert
    void addType(Type type);
    @Query("SELECT * FROM Type")
    List<Type> getAllTypes();

    @Insert
    void addDigimon(Digimon digimon);
    @Query("SELECT * FROM Digimon WHERE name = :name")
    Digimon getSpecificDigimon(String name);
    @Query("SELECT description FROM Digimon WHERE name = :name")
    String getDigimonDescription(String name);
    @Query("SELECT name FROM Digimon")
    List<String> getAllDigimonNames();
    @RawQuery
    List<String> getFilteredDigimon(SupportSQLiteQuery query);
}
