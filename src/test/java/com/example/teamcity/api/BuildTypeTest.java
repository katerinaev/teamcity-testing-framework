package com.example.teamcity.api;

import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.models.Roles;
import com.example.teamcity.api.models.User;
import com.example.teamcity.api.requests.CheckedRequests;
import com.example.teamcity.api.requests.checked.CheckedBase;
import com.example.teamcity.api.requests.unchecked.UncheckedBase;
import com.example.teamcity.api.spec.Specifications;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.example.teamcity.api.enums.Endpoint.*;
import static com.example.teamcity.api.generators.TestDataGenerator.generate;
import static io.qameta.allure.Allure.step;

@Test(groups = {"Regression"})
public class BuildTypeTest extends BaseApiTest {
    @Test(description = "User should be able to create build type", groups = {"Positive", "CRUD"})
    public void userCreatesBuildTypeTest() {
        step("Create user");
        superUserCheckRequests.getRequest(USERS).create(testData.getUser());
        var userCheckRequests = new CheckedRequests(Specifications.authSpec(testData.getUser()));

        step("Create project by user");
        userCheckRequests.<Project>getRequest(PROJECTS).create(testData.getProject());

        step("Create buildType for project by user");
        userCheckRequests.getRequest(BUILD_TYPES).create(testData.getBuildType());

        var createdBuildType = userCheckRequests.<BuildType>getRequest(BUILD_TYPES).read(testData.getBuildType().getId());

        step("Check buildType was created successfully");
        softy.assertEquals(testData.getBuildType().getName(), createdBuildType.getName(), "Build Type Name is not correct");
    }

    @Test(description = "User should not be able to create two build types with the same id", groups = {"Negative", "CRUD"})
    public void userCreatesTwoBuildTypesWithTheSameIdTest() {
        var buildTypeWithSameId = generate(Arrays.asList(testData.getProject()), BuildType.class, testData.getBuildType().getId());

        superUserCheckRequests.getRequest(USERS).create(testData.getUser());
        var userCheckRequests = new CheckedRequests(Specifications.authSpec(testData.getUser()));

        userCheckRequests.<Project>getRequest(PROJECTS).create(testData.getProject());

        userCheckRequests.getRequest(BUILD_TYPES).create(testData.getBuildType());
        new UncheckedBase(Specifications.authSpec(testData.getUser()), BUILD_TYPES)
                .create(buildTypeWithSameId)
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("The build configuration / template ID \"%s\" is already used by another configuration or template".formatted(testData.getBuildType().getId())));
//        step("Create user");
//        step("Create project by user");
//        step("Create buildType1 for project by user");
//        step("Create buildType2 with same id as buildType1 for project by user");
//        step("Check buildType2 was not created with bad request code");
    }

    @Test(description = "Project admin should be able to create build type for their project", groups = {"Positive", "Roles"})
    public void projectAdminCreatesBuildTypeTest() {
        step("Create project");
        superUserCheckRequests.getRequest(PROJECTS).create(testData.getProject());

        step("Create testData for User with Role and Project");
        testData.getUser().setRoles(generate(Roles.class, "PROJECT_ADMIN", "p:" + testData.getProject().getId()));

        step("Create User with these testData");
        superUserCheckRequests.<User>getRequest(USERS).create(testData.getUser());

        step("Create buildType for project by user (PROJECT_ADMIN)");
        var userCheckRequest = new CheckedRequests(Specifications.authSpec(testData.getUser()));
        userCheckRequest.getRequest(BUILD_TYPES).create(testData.getBuildType());

        step("Check buildType was created successfully");
        var createdBuildType = userCheckRequest.<BuildType>getRequest(BUILD_TYPES).read(testData.getBuildType().getId());
        softy.assertEquals(testData.getBuildType().getName(), createdBuildType.getName(), "Build Type Name is not correct");


//        step("Create user");
//        step("Create project");
//        step("Grant user PROJECT_ADMIN role in project");
//
//        step("Create buildType for project by user (PROJECT_ADMIN)");
//        step("Check buildType was created successfully");

    }

    @Test(description = "Project admin should not be able to create build type for not their project", groups = {"Negative", "Roles"})
    public void projectAdminCreatesBuildTypeForAnotherUserProjectTest() {
//        step("Create user1");
//        step("Create project1");
//        step("Grant user1 PROJECT_ADMIN role in project1");
//
//        step("Create user2");
//        step("Create project2");
//        step("Grant user2 PROJECT_ADMIN role in project2");
//
//        step("Create buildType for project1 by user2");
//        step("Check buildType was not created with forbidden code");
        step("Create project");
        superUserCheckRequests.getRequest(PROJECTS).create(testData.getProject());

        step("Create project2");
        var project2 = superUserCheckRequests.<Project>getRequest(PROJECTS).create(generate(Project.class));

        step("Create testData for user2");
        var user2 = generate(User.class);

        step("Create User2 with these testData");
        user2.setRoles(generate(Roles.class, "PROJECT_ADMIN", "p:" + project2.getId()));
        superUserCheckRequests.<User>getRequest(USERS).create(user2);

        step("Check buildType for project1 was not created by user2 with forbidden code");
        new UncheckedBase(Specifications.authSpec(user2), BUILD_TYPES)
                .create(testData.getBuildType())
                .then().assertThat().statusCode(HttpStatus.SC_FORBIDDEN);
    }
}
