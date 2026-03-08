package com.spinachtesters.spinachbooking.data

import com.spinachtesters.spinachbooking.data.repositories.FirebaseRepository
import com.spinachtesters.spinachbooking.data.repositories.UserRepository
import com.spinachtesters.spinachbooking.domain.models.User
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

class UserRepositoryTest {

    private lateinit var fakeSource: FirebaseRepository<User>
    private lateinit var repo: UserRepository
    private lateinit var testUser: User

    @BeforeEach
    fun setup() {
        fakeSource = mockk<FirebaseRepository<User>>()
        repo = UserRepository(fakeSource)

        testUser = User(
            id = "test123",
            fullName = "Test User",
            username = "username123",
            passwordHash = "hash",
            passwordSalt = "salt",
            passwordIterations = 210000,
            email = "email123@example.com",
            phoneNb = "1234567890",
            isOrganizer = false
        )
    }

    @Test
    @DisplayName("save delegates the action to FirebaseRepository")
    fun saveDelegatesToSource() = runTest {
        coEvery { fakeSource.save("test123", testUser) } just Runs

        repo.save("test123", testUser)

        coVerify(exactly = 1) { fakeSource.save("test123", testUser) }
    }

    @Test
    @DisplayName("getById returns the user if the id is found")
    fun getByIdReturnsUser() = runTest {
        coEvery { fakeSource.getById("test123") } returns testUser

        val result = repo.getById("test123")

        assertEquals(testUser, result)
        coVerify(exactly = 1) { fakeSource.getById("test123") }
    }

    @Test
    @DisplayName("getById returns null when user not found")
    fun getByIdReturnsNull() = runTest {
        coEvery { fakeSource.getById("missing") } returns null

        val result = repo.getById("missing")

        assertNull(result)
        coVerify(exactly = 1) { fakeSource.getById("missing") }
    }

    @Test
    @DisplayName("getAll returns a list of users")
    fun getAllReturnsUsers() = runTest {
        val users = listOf(testUser, testUser.copy(id = "other"))
        coEvery { fakeSource.getAll() } returns users

        val result = repo.getAll()

        assertEquals(users, result)
        coVerify(exactly = 1) { fakeSource.getAll() }
    }

    @Test
    @DisplayName("getAll returns an empty list")
    fun getAllReturnsEmptyList() = runTest {
        coEvery { fakeSource.getAll() } returns emptyList()

        val result = repo.getAll()

        assertEquals(emptyList<User>(), result)
        coVerify(exactly = 1) { fakeSource.getAll() }
    }

    @Test
    @DisplayName("usernameExists returns true when username already exists")
    fun usernameExistsReturnsTrue() = runTest {
        coEvery { fakeSource.getAll() } returns listOf(testUser)

        val result = repo.usernameExists("username123")

        Assertions.assertTrue(result)
    }

    @Test
    @DisplayName("emailExists returns false when email does not exist")
    fun emailExistsReturnsFalse() = runTest {
        coEvery { fakeSource.getAll() } returns listOf(testUser)

        val result = repo.emailExists("new@example.com")

        Assertions.assertFalse(result)
    }

    @Test
    @DisplayName("findByLoginIdentifier returns user for matching username")
    fun findByLoginIdentifierReturnsUser() = runTest {
        coEvery { fakeSource.getAll() } returns listOf(testUser)

        val result = repo.findByLoginIdentifier("username123")

        assertEquals(testUser, result)
    }

    @Test
    @DisplayName("findByLoginIdentifier returns null when no matches are found")
    fun findByLoginIdentifierReturnsNull() = runTest {
        coEvery { fakeSource.getAll() } returns listOf(testUser)

        val result = repo.findByLoginIdentifier("unknown")

        assertNull(result)
    }
}
