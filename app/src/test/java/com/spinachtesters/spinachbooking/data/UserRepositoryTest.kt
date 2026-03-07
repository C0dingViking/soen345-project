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
            username = "username123",
            password = "pass123",
            email = "email123",
            phoneNb = "phoneNb123",
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

        Assertions.assertEquals(testUser, result)
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

        Assertions.assertEquals(users, result)
        coVerify(exactly = 1) { fakeSource.getAll() }
    }

    @Test
    @DisplayName("getAll returns an empty list")
    fun getAllReturnsEmptyList() = runTest {
        coEvery { fakeSource.getAll() } returns emptyList()

        val result = repo.getAll()

        Assertions.assertEquals(emptyList<User>(), result)
        coVerify(exactly = 1) { fakeSource.getAll() }
    }

}
