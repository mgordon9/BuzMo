package com.oracle;

import java.sql.SQLException;
import java.util.*;

/**
 *
 */
public class Session {
    private User currentUser;

    public Session(User loggedUser) {
        currentUser = loggedUser;
    }

    public void start() {
        if(currentUser instanceof Manager)
            selectUserAction(true);
        else
            selectUserAction(false);
    }

//    private void selectManagerAction(){
//        Scanner scanner = new Scanner(java.lang.System.in);
//
//        boolean logout = false;
//        while(!logout) {
//            System.out.println(Constants.ManagerActionMenu);
//            String input = scanner.nextLine();
//            if (input.equalsIgnoreCase("p"))
//                createUserAction();
//            else if(input.equalsIgnoreCase("v"))
//                viewReportAction();
//            else if (input.equalsIgnoreCase("l"))
//                logout = true;
//            else
//                System.out.println("Invalid action");
//        }
//    }

//    private void viewReportAction() {
//        Manager manager = (Manager)currentUser;
//        int numNewMessages = manager.generateNumNewMessages();
//        int numMessageReads = manager.generateNumMessageReads();
//        int avgNumNewMessageReads = manager.generateAvgNumNewMessageReads();
//
//    }

    private void selectUserAction(boolean isManager) {
        Scanner scanner = new Scanner(java.lang.System.in);

        boolean logout = false;
        while(!logout) {
            System.out.println(Constants.UserActionMenu);
            if(isManager)
                System.out.println(Constants.ManagerActionMenu);
            else
                System.out.println();

            String input = scanner.nextLine();
            if(input.equalsIgnoreCase("g"))
                selectExistingChatgroup();
            else if(input.equalsIgnoreCase("c"))
                viewCircleAction();
            else if(input.equalsIgnoreCase("p"))
                privateMessageAction();
            else if(input.equalsIgnoreCase("a"))
                createChatGroupAction();
            else if(input.equalsIgnoreCase("m"))
                browseMessagesAction();
            else if(input.equalsIgnoreCase("u"))
                browseUsersAction();
            else if(input.equalsIgnoreCase("r"))
                viewRequestsAction();
            else if (input.equalsIgnoreCase("t") && isManager)
                createUserAction();
            else if(input.equalsIgnoreCase("l"))
                logout = true;
            else
                System.out.println("Invalid action");
        }
    }

    private void browseUsersAction() {
        Scanner scanner = new Scanner(System.in);

        BrowseUserSession browseUserSession = null;
        System.out.print("Search by User ID?(y/n): ");
        String useEmail = scanner.nextLine();
        if(useEmail.equalsIgnoreCase("y")) {
            System.out.print("Enter Users ID: ");
            String userID = scanner.nextLine();
            browseUserSession = new BrowseUserSession(userID);
        }
        else {
            System.out.println("Enter Topics delimited by a comma: ");
            String topicsInputString = scanner.nextLine();
            String [] topicsAsStrings = topicsInputString.replaceAll("\\s+","").split(",");

            ArrayList<Topic> topics = new ArrayList<Topic>();
            for(String topicString : topicsAsStrings)
                topics.add(new Topic(topicString));

            browseUserSession = new BrowseUserSession(topics);
        }

        ArrayList<User> browseUsers = browseUserSession.queryUsers();

        inBrowseUserOptions(browseUsers);
    }

    private void browseMessagesAction() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("What topics should be used to search? Delimit by comma:");
        String topicInputString = scanner.nextLine();
        String [] topicsAsStrings = topicInputString.replaceAll("\\s+","").split(",");

        ArrayList<Topic> topics = new ArrayList<Topic>();
        for(String topicString : topicsAsStrings)
            topics.add(new Topic(topicString));

        System.out.print("Should the messages match all of the topics?(y/n): ");
        String matchInput = scanner.nextLine();
        boolean matchAll = false;
        if(matchInput.equalsIgnoreCase("y"))
            matchAll = true;

        BrowseMessageSession browseMessageSession = new BrowseMessageSession(topics, matchAll);
        ArrayList<Message> browseMessages = browseMessageSession.queryMessages(new Date(), true);

        inBrowseMessageOptions(browseMessages, browseMessageSession);
    }

    private void inBrowseUserOptions(ArrayList<User> browseUsers) {
        Scanner scanner = new Scanner(System.in);

        boolean done = false;
        while(!done) {
            for(User user : browseUsers)
                System.out.println(user.getUserId());

            System.out.println(Constants.BrowseUserOptions);

            String option = scanner.nextLine();

            if (option.equals("1"))
                addFriend();
            else if (option.equals("2"))
                done = true;
            else
                System.out.println("Invalid Option");
        }

    }

    private void inBrowseMessageOptions(ArrayList<Message> browseMessages, MessageQueryable messageQueryable) {
        Scanner scanner = new Scanner(System.in);

        boolean done = false;
        while(!done) {
            for(Message message : browseMessages)
                System.out.println(message);

            System.out.println(Constants.BrowseOptionMenu);
            if(messageQueryable instanceof BrowseUserSession)
                System.out.println(Constants.BrowseUserOptions);

            String option = scanner.nextLine();
            if (option.equals("1"))
                browseMessages = scrollUp(browseMessages, messageQueryable);
            else if (option.equals("2"))
                browseMessages = scrollDown(browseMessages, messageQueryable);
            else if (option.equals("3"))
                done = true;
            else if (option.equals("4"))
                addFriend();
            else
                System.out.println("Invalid Option");
        }

    }

    private void addFriend() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter UserID: ");
        String userId = scanner.nextLine();
        if(currentUser.queryFriends().get(userId) == null)
            currentUser.addFriend(userId);
        else
            System.out.println("User is already your friend.");
    }

    private void viewRequestsAction() {
        Scanner scanner = new Scanner(System.in);
        ArrayList<Request> requests = currentUser.viewRequests();
        for(Request request : requests) {
            System.out.println(request.getType() + " : " + request.getAddTo().returnId());
            System.out.print("Accept?(y/n) ");
            String input = scanner.nextLine();
            if(input.equalsIgnoreCase("y"));
                request.acceptRequest();
        }
    }

    private void createChatGroupAction() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter ChatGroup Name: ");
        String groupName = scanner.nextLine();
        if(groupName.length() <= 0) {
            System.out.println("Invalid Group Name");
            return;
        }

        System.out.print("Enter ChatGroup message duration: ");
        String duration = scanner.nextLine();
        if(duration.length() <= 0) {
            System.out.println("Invalid Duration");
            return;
        }

        ChatGroup newChatgroup = new ChatGroup(currentUser.getUserId(), groupName, Integer.parseInt(duration));

        try {
            newChatgroup.createChatGroup(newChatgroup);
        } catch (SQLException e) {
            System.out.println(e.toString());
        }

        inChatGroupOptions(newChatgroup);
    }

    private void privateMessageAction() {
        Scanner scanner = new Scanner(System.in);

        HashMap<String, User> friends = currentUser.queryFriends();
        for(Map.Entry<String, User> friend : friends.entrySet())
            System.out.println(friend.getKey() + " : " + friend.getValue().getName());

        System.out.print("Select User by ID: ");
        String userId = scanner.nextLine();

        PrivateConversation privateConversation = new PrivateConversation(userId, currentUser.getUserId());

        Date dateQueryParam = new Date();
        ArrayList<Message> privateMessages = privateConversation.queryMessages(dateQueryParam, true);

        for(Message privateMessage : privateMessages)
            System.out.println(privateMessage.toString());

        inViewCircleAndPrivateOptions(privateMessages, Constants.insideViewCircleMenu, privateConversation);
    }

    private void viewCircleAction() {
        Scanner scanner = new Scanner(System.in);

        HashMap<String, User> friendsList = currentUser.queryFriends();
        System.out.println(currentUser.getUserId() + " : My Circle" );
        for (Map.Entry<String, User> friend : friendsList.entrySet())
            System.out.println(friend.getKey() + " : " + friend.getValue().getName());

        System.out.println("\nSelect User ID: ");
        String userId = scanner.nextLine();

        ArrayList<Message> circleMessages;
        if(userId.equals(currentUser.getUserId()))
            circleMessages = currentUser.queryMessages(new Date(), true);
        else {
            User circleUser = new User(userId);
            circleMessages = circleUser.queryMessages(new Date(), true);
        }

        if(circleMessages == null) {
            System.out.println("Invalid User ID");
            return;
        }

        for(Message circleMessage : circleMessages)
            System.out.println(circleMessage.toString());

        inViewCircleAndPrivateOptions(circleMessages, Constants.insidePrivateConversationMenu, currentUser);

    }

    private void inViewCircleAndPrivateOptions(ArrayList<Message> messages, String menu, MessageQueryable messageQueryable) {
        Scanner scanner = new Scanner(System.in);

        boolean done = false;
        while(!done) {
            System.out.println(menu);
            String option = scanner.nextLine();
            if (option.equals("1")) {
                Message message = createNewMessage();
                messageQueryable.postMessage(message);

                messages = messageQueryable.queryMessages(new Date(), true);
                for(Message queryMessage : messages)
                    System.out.println(queryMessage.toString());
            }
            else if (option.equals("2")) {
                messages = scrollUp(messages, messageQueryable);
                for(Message message : messages)
                    System.out.println(message.toString());
            }
            else if (option.equals("3")) {
                messages = scrollDown(messages, messageQueryable);
                for(Message message : messages)
                    System.out.println(message.toString());
            }
            else if (option.equals("4"))
                done = true;
            else
                System.out.println("Invalid Option");
        }

    }

//    private void postMessageToCircle() {
//        Message message = createNewMessage();
//
//        Scanner scanner = new Scanner(System.in);
//
//        HashMap<String, User> postToUsersCicle = new HashMap<String, User>();
//        System.out.println("Make message public?(y/n)");
//        String input = scanner.nextLine();
//        boolean isPublic = false;
//        if(input.equalsIgnoreCase("y")) {
//            isPublic = true;
//            postToUsersCicle = currentUser.queryFriends();
//        }
//        else {
//            HashMap<String, User> allFriends = currentUser.queryFriends();
//            for(Map.Entry<String, User> user : allFriends.entrySet())
//                System.out.println(user.getKey() + " : " + user.getValue().getName());
//
//            System.out.println("select friends to post in their circle. Delimit by a comma.");
//            String usersString = scanner.nextLine();
//            String [] userIds = usersString.replaceAll("\\s+","").split(",");
//
//            for( String userId : userIds)
//                postToUsersCicle.put(userId, new User(userId));
//        }
//
//        currentUser.postMessage(message, postToUsersCicle, isPublic);
//    }

    private void selectExistingChatgroup() {
        Scanner scanner = new Scanner(System.in);

        HashMap<String, ChatGroup> currentChatgroups = currentUser.queryChatGroups();

        for(Map.Entry<String, ChatGroup> chatgroup : currentChatgroups.entrySet())
            System.out.println(chatgroup.getKey() + " : " + chatgroup.getValue().getName());

        System.out.print("Enter ChatGroup ID: ");
        String chatgroupId = scanner.nextLine();

        System.out.println(chatgroupId);
        ChatGroup selectedChatgroup = currentChatgroups.get(chatgroupId);

        if(selectedChatgroup == null) {
            System.out.println("ChatGroup ID doesn't exist");
            return;
        }

        inChatGroupOptions(selectedChatgroup);

    }

    private void inChatGroupOptions(ChatGroup selectedChatgroup) {

        // prints out most recent messages in chatgroup
        Date queryDateParam = new Date();
        ArrayList<Message> messages = selectedChatgroup.queryMessages(queryDateParam, true);
        Collections.reverse(messages);
        for (Message message : messages)
            System.out.println(message.toString());


        Scanner scanner = new Scanner(System.in);
        boolean done = false;
        while(!done) {
            System.out.print(Constants.userChatGroupMessageOptions);
            if(currentUser.getUserId().equals(selectedChatgroup.getOwnerId()))
                System.out.println(Constants.ownerChatGroupOptions);
            else
                System.out.println();

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String option = scanner.nextLine();
            // post a new message to the chatgroup
            if(option.equals("1")) {
                Message newMessage = createNewMessage();
                selectedChatgroup.postMessage(newMessage);
            }
            // add a friend to the chat group
            else if(option.equals("2")) {
                HashMap<String, User> friendsList = currentUser.queryFriends();
                for (Map.Entry<String, User> friend : friendsList.entrySet())
                    System.out.println(friend.getKey() + " : " + friend.getValue().getName());

                System.out.print("Enter user ID: ");
                String userID = scanner.nextLine();
                User friendToAdd = friendsList.get(userID);
                if(friendToAdd == null) {
                    System.out.println("NOT FRIEND. BYE");
                    continue;
                }
                selectedChatgroup.addFriendToChatGroup(friendToAdd);
            }
            // scroll up (get next set of older messages)
            else if(option.equals("3")) {
                messages = scrollUp(messages, selectedChatgroup);
                for (Message message : messages)
                    System.out.println(message.toString());
            }
            // scroll down (get next set of newer messages)
            else if(option.equals("4")) {
                messages = scrollDown(messages, selectedChatgroup);
                for (Message message : messages)
                    System.out.println(message.toString());
            }
            // back to main menu
            else if(option.equals("5"))
                done = true;
            // update chatgroup message duration
            else if(option.equals("6") && currentUser.getUserId().equals(selectedChatgroup.getOwnerId())) {
                System.out.print("Enter new ChatGroup message duration(seconds): ");
                String newDuration = scanner.nextLine();
                int duration = Integer.parseInt(newDuration);
                selectedChatgroup.updateDuration(duration);
            }
            // update chatgroup name
            else if(option.equals("7") && currentUser.getUserId().equals(selectedChatgroup.getOwnerId())) {
                System.out.print("Enter new ChatGroup name: ");
                String newName = scanner.nextLine();
                try {
                    selectedChatgroup.updateName(newName);
                } catch (SQLException e) {
                    System.out.println(e.toString());
                }
            }
            else
                System.out.println("INVALID OPTION\n");
        }
    }

    private ArrayList<Message> scrollDown(ArrayList<Message> messages, MessageQueryable messageQueryable) {
        if(messages.size() <= 0)
            return messages;

        Date queryDateParam = messages.get(messages.size() - 1).getDatePosted();
        ArrayList<Message> newMessages = messageQueryable.queryMessages(queryDateParam, false);
        return newMessages;
    }

    private ArrayList<Message> scrollUp(ArrayList<Message> messages, MessageQueryable messageQueryable) {
        if(messages.size() <= 0)
            return messages;

        Date queryDateParam = messages.get(0).getDatePosted();
        ArrayList<Message> newMessages = messageQueryable.queryMessages(queryDateParam, true);
        Collections.reverse(newMessages);
        return newMessages;
    }

    private Message createNewMessage() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter Messsage:");
        String messageString = scanner.nextLine();
        Message message =  new Message(currentUser.getUserId(), messageString);
        message.storeMessage();
        return  message;
    }

    private void createUserAction(){
        Scanner scanner = new Scanner(System.in);
        String username = "";
        String password = "";
        try {
            System.out.print("Enter new Username: ");
            username = scanner.nextLine();

            System.out.print("Enter Password: ");
            password = scanner.nextLine();

            System.out.print("Enter Password again: ");
            String secondPass = scanner.nextLine();

            if(!validateUser(username, password, secondPass))
                return;

            createUser(username, password);
            System.out.println("Successfully added user " + username);

        } catch (SQLException e) {
            System.out.println("Could not create user " + username + "\n" + e.getMessage());
        }
    }

    private void createUser(String username, String password) throws SQLException {

        if(currentUser instanceof Manager) {
            Manager manager = (Manager)currentUser;
            manager.addUser(username, password);
        }
        else
            System.out.println("User has invalid privileges to create another user.");
    }

    private boolean validateUser(String username, String password, String secondPass) {
        if(!password.equals(secondPass)) {
            System.out.println("\nPasswords do not match\n");
            return false;
        }
        else if(password.length() <= 0) {
            System.out.println("\nPassword invalid\n");
            return false;
        }

        return true;
    }
}
