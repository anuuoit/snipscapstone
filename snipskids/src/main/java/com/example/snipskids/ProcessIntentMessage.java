package com.example.snipskids;

import android.util.Log;

import java.util.List;

import ai.snips.hermes.IntentMessage;
import ai.snips.hermes.Slot;
import ai.snips.nlu.ontology.SlotValue;

public class ProcessIntentMessage {

    private static ProcessIntentMessage intentMessage = null;

    private ProcessIntentMessage(){

    }
    public static ProcessIntentMessage getInstance()
    {
        if (intentMessage == null)
            intentMessage = new ProcessIntentMessage();

        return intentMessage;
    }

    public Response process(IntentMessage intentMessage){

        String intentName = intentMessage.getIntent().getIntentName();
        Log.d("Anu", intentName);
        if (intentName.equals("anuprakash:add")
                || intentName.equals("anuprakash:minus")
                || intentName.equals("anuprakash:multiply")
                || intentName.equals("anuprakash:divide")) {
            return processMath(intentName, intentMessage.getSlots());

        } else if (intentName.equals("anuprakash:playitem")) {
            return processPlay(intentMessage.getSlots());

        } else if (intentName.equals("anuprakash:tell")) {
            return processTell(intentMessage.getSlots());

        } else if (intentName.equals("anuprakash:greetings")) {
            return processGreetings(intentMessage.getSlots());

        } else if (intentName.equals("anuprakash:questions")) {
            return processQuestions(intentMessage.getSlots());
        }
        return new Response(R.drawable.confused, R.string.resp_not_understand);
    }


    private Response processMath(String intentName, List<Slot> slots){
        Response response = null;
        int firstNum = -1;
        int secondNum = -1;
        for (Slot slot : slots) {

            if (slot.getValue() instanceof SlotValue.NumberValue) {
                double value = ((SlotValue.NumberValue) slot.getValue()).getValue();
                Log.d("Anu", String.valueOf(value));
                if (slot.getSlotName().equals("firstNumber")) {
                    firstNum = (int) value;
                } else if (slot.getSlotName().equals("secondNumber")) {
                    secondNum = (int) value;
                }
            }
        }
        if (firstNum != -1 && secondNum != -1) {
            Log.d("Anu", "firstNumber " + firstNum);
            Log.d("Anu", "secondNumber " + secondNum);
            int answer = 0;
            if (intentName.equals("anuprakash:add")) {
                answer = firstNum + secondNum;
            } else if (intentName.equals("anuprakash:minus")) {
                answer = firstNum - secondNum;
            } else if (intentName.equals("anuprakash:multiply")) {
                answer = firstNum * secondNum;
            } else {
                answer = firstNum / secondNum;
            }
            Log.d("Anu", "answer " + answer);
            response = new Response(R.drawable.math, "It is " +answer);
        }
        //TODO
        return response;
    }

    private Response processPlay(List<Slot> slots){
        Response response = null;
        for (Slot slot : slots) {

            if (slot.getSlotName().equals("item")) {
                if (slot.getValue() instanceof SlotValue.CustomValue) {
                    String value = ((SlotValue.CustomValue) slot.getValue()).getValue();
                    Log.d("Anu", value);
                    if (value.equals("music")) {
                        response = new Response(R.drawable.music, R.string.resp_music, Response.Action.MUSIC);
                    }
                }
            }
        }
        return response;
    }

    private  Response processTell(List<Slot> slots) {
        Response response = null;
        for (Slot slot : slots) {

            if (slot.getSlotName().equals("item")) {
                if (slot.getValue() instanceof SlotValue.CustomValue) {
                    String value = ((SlotValue.CustomValue) slot.getValue()).getValue();
                    Log.d("Anu", value);
                    if (value.equals("story")) {
                        response = new Response(R.drawable.story, R.string.resp_story, Response.Action.STORY);
                    } else if (value.equals("joke")) {
                        response = new Response(R.drawable.joke, R.string.resp_joke1);
                    } else if (value.equals("fruits")) {
                        response = new Response(R.drawable.fruits, R.string.resp_fruits);
                    } else if (value.equals("vegetables")) {
                        response = new Response(R.drawable.vegetables, R.string.resp_vegetables);
                    } else if (value.equals("animals")) {
                        response = new Response(R.drawable.animals, R.string.resp_animals);
                    }
                }
            }
        }
        return response;
    }

    private Response processGreetings(List<Slot> slots) {
        Response response = null;
        for (Slot slot : slots) {

            if (slot.getSlotName().equals("greeting")) {
                if (slot.getValue() instanceof SlotValue.CustomValue) {
                    String value = ((SlotValue.CustomValue) slot.getValue()).getValue();
                    Log.d("Anu", value);
                    if (value.equalsIgnoreCase("Good morning")) {
                        response = new Response(R.drawable.sun, R.string.resp_good_morning, Response.Action.NONE);
                    } else if (value.equals("Good night")) {
                        response = new Response(R.drawable.moon, R.string.resp_good_night);
                    } else if (value.equals("bye")) {
                        response = new Response(R.drawable.bye, R.string.resp_bye);
                    } else if (value.equals("hello") || (value.equals("hi"))) {
                        response = new Response(R.drawable.questions, R.string.resp_hello);
                    } else if (value.equalsIgnoreCase("Sweet dreams")) {
                        response = new Response(R.drawable.moon, R.string.resp_sweet_dreams);
                    }
                }
            }
        }
        return response;
    }

    private Response processQuestions(List<Slot> slots) {
        Response response = null;
        for (Slot slot : slots) {

            if (slot.getSlotName().equals("question")) {
                if (slot.getValue() instanceof SlotValue.CustomValue) {
                    String value = ((SlotValue.CustomValue) slot.getValue()).getValue();
                    Log.d("Anu", value);
                    if (value.equalsIgnoreCase("How are you?")) {
                        response = new Response(R.drawable.questions, R.string.resp_how_are_you);
                    } else if (value.equalsIgnoreCase("What is your name?")) {
                        response = new Response(R.drawable.questions, R.string.resp_what_is_your_name);
                    }
                }
            }
        }

        return response;
    }
}
