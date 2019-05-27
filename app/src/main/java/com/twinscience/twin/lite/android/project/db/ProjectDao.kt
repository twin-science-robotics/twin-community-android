package com.twinscience.twin.lite.android.project.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update
import com.twinscience.twin.lite.android.project.data.ProjectEntity

@Dao
interface ProjectDao {

    @Query("select * from projectentity")
    fun getAllProjects(): List<ProjectEntity>

    @Query("select * from projectentity where id = :id")
    fun findProjectById(id: Long): ProjectEntity

    @Insert(onConflict = REPLACE)
    fun insertProject(projectentity: ProjectEntity)

    @Update(onConflict = REPLACE)
    fun updateProject(projectentity: ProjectEntity)
/*
    @Query("UPDATE ProjectEntity SET name= :name  SET id= :id where id = :id")
    fun updateProject(name: String, id: Long)*/

    @Query("delete from projectentity where id = :id")
    fun deleteProject(id: Long?)
}