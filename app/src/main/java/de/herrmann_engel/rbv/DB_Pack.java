package de.herrmann_engel.rbv;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DB_Pack {

        @PrimaryKey (autoGenerate = true)
        public int uid;

        @ColumnInfo(name = "name")
        public String name;

        @ColumnInfo(name = "desc")
        public String desc;

        @ColumnInfo(name = "date")
        public long date;

        @ColumnInfo(name = "colors")
        public int colors;

        @ColumnInfo(name = "collection")
        public int collection;

}
