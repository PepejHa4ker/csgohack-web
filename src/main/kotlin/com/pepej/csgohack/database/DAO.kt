package com.pepej.csgohack.database

import com.pepej.csgohack.sql
import com.pepej.csgohack.view.RequestUser
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object Users : IntIdTable() {
    val hwid = varchar("hwid", 100)
    val ip = varchar("ip", 16)
    val key = varchar("key", 64)
    val subscriptionStartDate = long("subscription_start_date")
    val subscriptionEndDate = long("subscription_end_date")
    val cookies = text("cookies") // :)
}

object Keys : IntIdTable() {
    val key = varchar("key", 64)
    val subscriptionDuration = long("subscription_duration")
    val active = bool("active")

}


class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserEntity>(Users)

    fun toRequestUser(): RequestUser {
        return RequestUser(hwid, ip, key, subscriptionStartDate, subscriptionEndDate, cookies)
    }

    var hwid by Users.hwid
    var ip by Users.ip
    var key by Users.key
    var subscriptionStartDate by Users.subscriptionStartDate
    var subscriptionEndDate by Users.subscriptionEndDate
    var cookies by Users.cookies


}

class KeyEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<KeyEntity>(Keys)
    var key by Keys.key
    var subscriptionDuration by Keys.subscriptionDuration
    var active by Keys.active
}


class SQLManager {

    private val datasource: HikariDataSource
    private val database: Database

    init {
        val config = HikariConfig().apply {
            dataSourceClassName = "com.mysql.cj.jdbc.MysqlDataSource"
            jdbcUrl = "jbdc:mysql://localhost:3306/localhost"
            addDataSourceProperty("serverName", "localhost")
            addDataSourceProperty("port", 3306)
            addDataSourceProperty("databaseName", "localtest")
            addDataSourceProperty("user", "root")
            addDataSourceProperty("password", "")
            addDataSourceProperty("useSSL", false)
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        }
        datasource = HikariDataSource(config)
        database = Database.connect(datasource)
        transaction {
            SchemaUtils.create (Users, Keys)
        }

    }


    suspend fun newUser(
        hwid: String,
        key: String,
        ip: String,
        subscriptionStartDate: Long,
        subscriptionEndDate: Long,
        cookies: String,
    ) = newSuspendedTransaction(Dispatchers.IO, database) {
        UserEntity.new {
            this@new.hwid = hwid
            this@new.key = key
            this@new.ip = ip
            this@new.subscriptionStartDate = subscriptionStartDate
            this@new.subscriptionEndDate = subscriptionEndDate
            this@new.cookies = cookies
        }
    }

    suspend fun userExists(hwid: String): Boolean = newSuspendedTransaction(Dispatchers.IO, database) {
        getUserByHwid(hwid) != null
    }

    suspend fun getUserByHwid(hwid: String): UserEntity? = newSuspendedTransaction(Dispatchers.IO, database) {
        UserEntity.find { Users.hwid eq hwid.toLowerCase() }.firstOrNull()
    }

    suspend fun getUserByKey(key: String): UserEntity? = newSuspendedTransaction(Dispatchers.IO, database) {
        UserEntity.find { Users.key eq key.toLowerCase() }.firstOrNull()
    }

    suspend fun getUsersByIp(ip: String): List<UserEntity?> = newSuspendedTransaction(Dispatchers.IO, database) {
        UserEntity.find { Users.ip eq ip.toLowerCase() }.toList()

    }

    suspend fun generateKey(key: String, subscriptionDuration: Long): KeyEntity =
        newSuspendedTransaction(Dispatchers.IO, database) {
            KeyEntity.new {
                this@new.key = key
                this@new.subscriptionDuration = subscriptionDuration
                this@new.active = true
            }
        }

    suspend fun keyExist(key: String): Boolean =
        newSuspendedTransaction(Dispatchers.IO, database) {
            KeyEntity.find { Keys.key eq key.toLowerCase() }.firstOrNull() != null
        }


}

class UserManager {
    private val job by lazy { Job() }
    private val coroutineScope by lazy { CoroutineScope(job) }
    private val sqlManager get() = sql


    suspend fun createUser(
        hwid: String,
        key: String,
        ip: String,
        subscriptionStartDate: Long,
        subscriptionEndDate: Long,
        cookies: String,
    ) = coroutineScope.async {
        return@async sqlManager.newUser(hwid, key, ip, subscriptionStartDate, subscriptionEndDate, cookies)
    }.await()


    suspend fun userExists(hwid: String): Boolean =
        coroutineScope.async {
            return@async sqlManager.userExists(hwid)
        }.await()

    suspend fun getUserByHwid(hwid: String): UserEntity? =
        coroutineScope.async {
            return@async sqlManager.getUserByHwid(hwid)
        }.await()

    suspend fun getUserByKey(key: String): UserEntity? =
        coroutineScope.async {
            return@async sqlManager.getUserByKey(key)
        }.await()

    suspend fun getUsersByIp(ip: String): List<UserEntity?> =
        coroutineScope.async {
            return@async sqlManager.getUsersByIp(ip)
        }.await()
}

class KeyManager {
    private val job by lazy { Job() }
    private val coroutineScope by lazy { CoroutineScope(job) }
    private val sqlManager get() = sql

    suspend fun generateKey(key: String, subscriptionDuration: Long): KeyEntity =
        coroutineScope.async {
            return@async sqlManager.generateKey(key, subscriptionDuration)
        }.await()

    suspend fun keyExist(key: String): Boolean =
        coroutineScope.async {
            return@async sqlManager.keyExist(key)
        }.await()


}