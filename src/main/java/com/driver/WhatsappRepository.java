package com.driver;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class WhatsappRepository {

    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public String createUser(String name, String mobile) {
        if(userMobile.contains(mobile)){
            throw new RuntimeException("User already exists");
        }
        userMobile.add(mobile);
        return "SUCCESS";
    }

    public Group createGroup(List<User> users) {
        String groupName;
        if(users.size() == 2){
            groupName = users.get(1).getName();
        }else{
            customGroupCount++;
            groupName = "Group " + customGroupCount;
        }
        Group newGroup = new Group(groupName, users.size());

        groupUserMap.put(newGroup, users);
        adminMap.put(newGroup, users.get(0));

        return newGroup;
    }

    public int createMessage(String content) {
        messageId++;
        return messageId;
    }

    public int sendMessage(Message message, User sender, Group group) {
        if(!groupUserMap.containsKey(group)){
            throw new RuntimeException("Group does not exist");
        }

        List<User> groupMembers = groupUserMap.get(group);
        if(!groupMembers.contains(sender)){
            throw new RuntimeException("You are not allowed to send message");
        }

        List<Message> messages = new ArrayList<>();
        if(groupMessageMap.containsKey(group)){
            messages = groupMessageMap.get(group);
        }

        messages.add(message);
        groupMessageMap.put(group, messages);
        senderMap.put(message, sender);

        return messages.size();
    }

    public String changeAdmin(User approver, User user, Group group) {
        if(!groupUserMap.containsKey(group)){
            throw new RuntimeException("Group does not exist");
        }

        if(adminMap.get(group) != approver){
            throw new RuntimeException("Approver does not have rights");
        }

        List<User> groupMembers = groupUserMap.get(group);
        if(!groupMembers.contains(user)){
            throw new RuntimeException("User is not a participant");
        }

        adminMap.put(group, user);

        return "SUCCESS";
    }

    public int removeUser(User user) {
        boolean userFound = false;
        Group userGroup = null;
        for(Group currGroup : groupUserMap.keySet()){
            List<User> currGroupMembers = groupUserMap.get(currGroup);
            if(currGroupMembers.contains(user)){
                userFound = true;
                userGroup = currGroup;
//                break;
            }
        }
        if(!userFound){
            throw new RuntimeException("User not found");
        }

        if(adminMap.get(userGroup) == user){
            throw new RuntimeException("Cannot remove admin");
        }

        groupUserMap.get(userGroup).remove(user);

        List<Message> listOfMessagesToBeRemoved = new ArrayList<>();
        for(Message currMessage : senderMap.keySet()){
            if(senderMap.get(currMessage) == user){
                listOfMessagesToBeRemoved.add(currMessage);
            }
        }

        for(Message message : listOfMessagesToBeRemoved){
            groupMessageMap.get(userGroup).remove(message);
            senderMap.remove(message);
        }

        userMobile.remove(user.getMobile());
        userGroup.setNumberOfParticipants(groupUserMap.get(userGroup).size());

        return groupUserMap.get(userGroup).size() + groupMessageMap.get(userGroup).size() + senderMap.size();
    }

    public String findMessage(Date start, Date end, int k) {
        TreeMap<Date, Message> datesMessageDB = new TreeMap<>();
        for(Message currMessage : senderMap.keySet()){
            Date currMessageDate = currMessage.getTimestamp();
            if(currMessageDate.after(start) && currMessageDate.before(end)){
                datesMessageDB.put(currMessageDate, currMessage);
            }
        }
        if(datesMessageDB.size() < k){
            throw new RuntimeException("K is greater than the number of messages");
        }

        int count = 0;
        for(Date key : datesMessageDB.keySet()){
            if(count == (datesMessageDB.size() - k)){
                return datesMessageDB.get(key).getContent();
            }
            count++;
        }
        return "";
    }
}