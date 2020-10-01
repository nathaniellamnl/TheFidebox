
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
exports.cvGiggleNotification = functions.firestore.document('users/{poster_id}/CVs/{cv_id}/giggles/{giggle_user_id}').
  onCreate(async (snap, context) => {

    var poster_id = context.params.poster_id;
    const giggle_user_id = context.params.giggle_user_id;
    const cv_id = context.params.cv_id;
    const eventId=context.eventId;

    //Make sure no duplicate
    try {
      const previousEvent= await admin.firestore().collection('users').doc(poster_id).collection('--stat--').doc("notifications").get();

      if(previousEvent.data().cv_giggle_notifications_add_event===eventId){
        return console.log("Duplicate");
      }

     } catch (error) {

       console.log(error+"1");
     }


   // if not self-giggle, then send
    if (poster_id !== giggle_user_id) {

      console.log("poster not the same");
  // Get the list of device notification tokens.
  const getDeviceTokensPromise = admin.firestore().collection('users').doc(poster_id).collection('token').get();
  const getCVPromise = admin.firestore().collection('users').doc(poster_id).collection('CVs').
    doc(cv_id).get();

  const results = await Promise.all([getDeviceTokensPromise, getCVPromise]);

   // The snapshot to the user's tokens.
   const tokensSnapshot = results[0];
   var cv = results[1];

  //write the notification to the user collection
    var notification={
    cvId:cv.data().cvId
   }

  await sendNotification(tokensSnapshot,cv,poster_id);
    
  }
 
     //run Transaction
     await admin.firestore().runTransaction(async t =>{

     //check if the notification already exists   
     try {
      var cvGiggleNotification= await t.get(admin.firestore().collection('users').doc(poster_id).collection('cv_giggle_notifications').doc(cv_id));
      } catch (error) {

     console.log(error);
     }

     if(!cvGiggleNotification.exists && poster_id !== giggle_user_id){

      //update notifications  

      console.log("Trying to send notifications");
  
      let cvGiggleRef= admin.firestore().collection('users').doc(poster_id).collection('cv_giggle_notifications').doc(cv_id);  
      let cvGiggleCountRef=admin.firestore().collection('users').doc(poster_id).collection('--stat--').doc("notifications");
      
      //increase giggle count of the cv   
      let cvGiggleIncrementRef=admin.firestore().collection('users').doc(poster_id).collection('CVs').doc(cv_id);

      await Promise.all([t.set(cvGiggleRef,notification),  
                          t.update(cvGiggleCountRef,{cv_giggle_notifications: admin.firestore.FieldValue.increment(1),cv_giggle_notifications_add_event:eventId}),
                        t.update(cvGiggleIncrementRef,{giggles: admin.firestore.FieldValue.increment(1)})])
      } else{

        console.log("Will not send notifications");

      await admin.firestore().collection('users').doc(poster_id).collection('CVs').doc(cv_id).update({giggles: admin.firestore.FieldValue.increment(1)});
  
      }

  })
  });



async function sendNotification(tokensSnapshot,cv,poster_id){
  

   // Check if there are any device tokens.
   if (tokensSnapshot.size === 0) {
     return console.log('There are no notification tokens to send to.');
   }
   console.log('There are', tokensSnapshot.size, 'tokens to send notifications to.');
   
   const tokens = [];
   tokensSnapshot.forEach( function(doc) {
       const tokenId = doc.data().tokenId;
       tokens.push(tokenId)
     }
   )


   const message = {
     //By defalut,notification messages are collapsible
     data: {
       category:cv.data().category,
       title: 'Fidebox',
       body: `Your CV has ${cv.data().giggles+1} giggles and ${cv.data().comments} comments`,
       cv_id:cv.data().cvId,
       type:'CV',
       cv_giggles: `${cv.data().giggles+1}`,
       cv_comments:`${cv.data().comments}`,
       notifyee_id:poster_id
     }
   };

   const response = await admin.messaging().sendToDevice(tokens, message);
   // For each message check if there was an error.
   const tokensToRemove = [];
   response.results.forEach((result, index) => {
     const error = result.error;
     if (error) {
       console.error('Failure sending notification to', tokens[index], error);
       // Cleanup the tokens who are not registered anymore.
       if (error.code === 'messaging/invalid-registration-token' ||
         error.code === 'messaging/registration-token-not-registered') {
         tokensToRemove.push(admin.firestore().collection('users').doc(poster_id).collection('token')
           .doc(tokens[index]).delete());
       }
     }
   });
   await Promise.all(tokensToRemove);
}  


  
//No need as onAddCVCommentOrGiggle0 will increment the user notification number and send the notification to the user
// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// exports.cvCommentNotification = functions.firestore.document('users/{poster_id}/CVs/{cv_id}/comments/{commenter_id}').
//   onCreate(async (snap, context) => {

//     var poster_id = context.params.poster_id;
//     const commenter_id = context.params.commenter_id;
//     const cv_id = context.params.cv_id;
//     const eventId=context.eventId;

//     // if not self-giggle, then send
//     if (poster_id !== commenter_id) {

//       console.log("poster not the same");
//   // Get the list of device notification tokens.
//   const getDeviceTokensPromise = admin.firestore().collection('users').doc(poster_id).collection('token').get();
//   const getCVPromise = admin.firestore().collection('users').doc(poster_id).collection('CVs').
//     doc(cv_id).get();

//   const results = await Promise.all([getDeviceTokensPromise, getCVPromise]);

//    // The snapshot to the user's tokens.
//    const tokensSnapshot = results[0];
//    var cv = results[1];

//   //write the notification to the user collection
//     var notification={
//     cvId:cv.data().cvId
//    }
//    sendNotification(tokensSnapshot,cv,poster_id); }

  
//     //Make sure no duplicate
//     try {
//       const previousEvent= await admin.firestore().collection('users').doc(poster_id).collection('--stat--').doc("notifications").get();

//       if(previousEvent.data().cv_comment_notifications_add_event===eventId){
//         return console.log("Duplicate");
//       }

//      } catch (error) {
 
//        console.log(error+"1");
//      }
 
//      //run Transaction
//      await admin.firestore().runTransaction(async t =>{

//      //check if the notification already exists   
//      try {
//       var cvCommentNotification= await t.get(admin.firestore().collection('users').doc(poster_id).collection('cv_comment_notifications').doc(cv_id));
//       } catch (error) {

//      console.log(error);
//      }

//      if(!cvCommentNotification.exists && poster_id !== commenter_id){

//       //update notifications  

//       console.log("Trying to send notifications");
  
//       let cvCommentRef= admin.firestore().collection('users').doc(poster_id).collection('cv_comment_notifications').doc(cv_id);  
//       let cvCommentCountRef=admin.firestore().collection('users').doc(poster_id).collection('--stat--').doc("notifications");
      
//       //increase giggle count of the cv   
//       let cvCommentIncrementRef=admin.firestore().collection('users').doc(poster_id).collection('CVs').doc(cv_id);

//       await Promise.all([t.set(cvCommentRef,notification),  
//                           t.update(cvCommentCountRef,{cv_comment_notifications: admin.firestore.FieldValue.increment(1),cv_comment_notifications_add_event:eventId}),
//                         t.update(cvCommentIncrementRef,{comments: admin.firestore.FieldValue.increment(1)})])
//       } else{

//         console.log("Will not send notifications");

//       await admin.firestore().collection('users').doc(poster_id).collection('CVs').doc(cv_id).update({comments: admin.firestore.FieldValue.increment(1)});
  
//       }

//   })
//   });


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
exports.cvGiggleDeletion = functions.firestore.document('users/{poster_id}/CVs/{cv_id}/giggles/{giggle_user_id}').
onDelete(async (snap, context) => {

  const poster_id = context.params.poster_id;
  const cv_id = context.params.cv_id;
  const eventId=context.eventId;

  return onDeleteUserCVGiggle0(poster_id,cv_id,eventId);
});


async function onDeleteUserCVGiggle0(poster_id,cv_id,eventId){
  try{
    var cv= (await admin.firestore().collection('users').doc(poster_id).collection('CVs').doc(cv_id).get()).data();

     if(cv.giggleDeleteEventId===eventId){
       return;
     }
  
    return admin.firestore().collection('users').doc(poster_id).collection('CVs').doc(cv_id).update({giggles: admin.firestore.FieldValue.increment(-1),giggleDeleteEventId:eventId});
 
  } catch(error){
    console.log(error);
  }
 }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  exports.userCommentGiggleNotification = functions.firestore.document('users/{commenter_id}/comments/{comment_id}/giggles/{giggler_id}').
  onCreate(async (snap, context)=> {

    const commenter_id = context.params.commenter_id;
    const comment_id = context.params.comment_id;
    const giggler_id=context.params.giggler_id;
    const eventId=context.eventId;

    var selfGiggle=true;

      //Make sure no duplicate
       const previousEvent= await admin.firestore().collection('users').doc(commenter_id).collection('--stat--').doc("notifications").get();

        try{
        if(previousEvent.data().comment_giggle_notifications_eventId===eventId){
          return console.log("Duplicate");
        }
      } catch(error){
        console.log(error);
      }

      const getCommentPromise =await admin.firestore().collection('users').doc(commenter_id).collection('comments').doc(comment_id).get();  
      const comment = getCommentPromise;
      const route=comment.data().route;

      const routeString=route.split("/");
      const getCVPromise = admin.firestore().collection(routeString[0]).doc(routeString[1]).get();
      const cv=await getCVPromise;

    //if not self-comment
    if (commenter_id !== giggler_id) {
    
    selfGiggle=false;  
    const getDeviceTokensPromise =await admin.firestore().collection('users').doc(commenter_id).collection('token').get();

    // The snapshot to the user's tokens.
    const tokensSnapshot = getDeviceTokensPromise;

    //write the notification to the user collection
    var notification={
      cvId:`${cv.data().cvId}`,
      commentId:comment_id
    }

    // Check if there are any device tokens.
    if (tokensSnapshot.size === 0) {
      return console.log('There are no notification tokens to send to.');
    }

    console.log('There are', tokensSnapshot.size, 'tokens to send notifications to.');

    const tokens = [];
    tokensSnapshot.forEach( function(doc) {
        const tokenId = doc.data().tokenId;
        tokens.push(tokenId)
      }
    )

    const message = {
      //By defalut,notification messages are collapsible
      data: {
        category:cv.data().category,
        title: 'Fidebox',
        body: `Your comment has ${comment.data().giggles+1} giggles and ${comment.data().child_comments} replies`,
        route: `${comment.data().route}`,
        type:'comment',
        comment_id:comment_id,
        cv_id:cv.data().cvId,
        cv_giggles: `${cv.data().giggles+1}`,
        cv_comments:`${cv.data().comments}`,
        notifyee_id:commenter_id
      }
    };

    const response = await admin.messaging().sendToDevice(tokens, message);
    // For each message check if there was an error.
    const tokensToRemove = [];
    response.results.forEach((result, index) => {
      const error = result.error;
      if (error) {
        console.error('Failure sending notification to', tokens[index], error);
        // Cleanup the tokens who are not registered anymore.
        if (error.code === 'messaging/invalid-registration-token' ||
          error.code === 'messaging/registration-token-not-registered') {
          tokensToRemove.push(admin.firestore().collection('users').doc(commenter_id).collection('token')
            .doc(tokens[index]).delete());
        }
      }
    });
    await Promise.all(tokensToRemove);
    }
      
      await admin.firestore().runTransaction(async t =>{

        console.log("Haha");
     //check if the notification already exists   
    
      const commentGiggleNotification= await t.get(admin.firestore().collection('users').doc(commenter_id).collection('comment_giggle_notifications').doc(cv.data().cvId));

     if(!commentGiggleNotification.exists && !selfGiggle){

      //update notifications  

      let commentGiggleRef= admin.firestore().collection('users').doc(commenter_id).collection('comment_giggle_notifications').doc(cv.data().cvId);  
      let commentGiggleCountRef=admin.firestore().collection('users').doc(commenter_id).collection('--stat--').doc("notifications");
        //increase giggle count of the comment
      let commentGiggleIncrementRef=admin.firestore().collection('users').doc(commenter_id).collection('comments').doc(comment_id);

      console.log("Hello");

      return Promise.all([t.set(commentGiggleRef,notification),
              t.update(commentGiggleCountRef,{comment_giggle_notifications: admin.firestore.FieldValue.increment(1),comment_giggle_notifications_eventId:eventId}),       
                        t.update(commentGiggleIncrementRef,{giggles: admin.firestore.FieldValue.increment(1)})])
      } else{
       return admin.firestore().collection('users').doc(commenter_id).collection('comments').doc(comment_id).update({giggles: admin.firestore.FieldValue.increment(1)});
      }
  })

  
  })


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
exports.commentGiggleDeletion = functions.firestore.document('users/{poster_id}/comments/{comment_id}/giggles/{gigglerId}').
onDelete(async (snap, context) => {

  const poster_id = context.params.poster_id;
  const comment_id = context.params.comment_id;
  const eventId=context.eventId;

  return onDeleteUserCommnetGiggle0(poster_id,comment_id,eventId);

});


async function onDeleteUserCommnetGiggle0(poster_id,comment_id,eventId){
  try{
    var cv= (await admin.firestore().collection('users').doc(poster_id).collection('comments').doc(comment_id).get()).data();

     if(cv.giggleEventId===eventId){
       return;
     }
  
    return admin.firestore().collection('users').doc(poster_id).collection('comments').doc(comment_id).update({giggles: admin.firestore.FieldValue.increment(-1),giggleEventId:eventId});
 
  } catch(error){
    console.log(error);
  }
 }


  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  exports.userCommentReplyNotification = functions.firestore.document('users/{commenter_id}/comments/{comment_id}/comments/{reply_comment_id}').
   onCreate(async (snap, context)=> {
    
    const commenter_id = context.params.commenter_id;
    const comment_id = context.params.comment_id;
    const reply_comment=snap.data();
    const eventId=context.eventId;
    const reply_comment_id=context.params.reply_comment_id;

      //Make sure no duplicate
      try {
        const previousEvent= await admin.firestore().collection('users').doc(commenter_id).collection('--stat--').doc("notifications").get();
  
        if(previousEvent.data().comment_reply_notifications_eventId===eventId){
          return console.log("Duplicate");
        }
  
       } catch (error) {
   
         console.log(error);
       }

    const getCommentPromise =await admin.firestore().collection('users').doc(commenter_id).collection('comments').doc(comment_id).get();
    const comment = getCommentPromise;

    const routeString=comment.data().route.split("/");
    const getCVPromise = admin.firestore().collection(routeString[0]).doc(routeString[1]).get();
    const cv=await getCVPromise;

    console.log("neil");
    if (commenter_id !== reply_comment.commenterId) {
      
    const getDeviceTokensPromise =await admin.firestore().collection('users').doc(commenter_id).collection('token').get();

    // The snapshot to the user's tokens.
    const tokensSnapshot = getDeviceTokensPromise;

     //write the notification to the user collection
     var notification={
          cvId:`${cv.data().cvId}`,
          commentId:comment_id
        }

          // Check if there are any device tokens.
    if (tokensSnapshot.size === 0) {
      return console.log('There are no notification tokens to send to.');
    }
    console.log('There are', tokensSnapshot.size, 'tokens to send notifications to.');

    const tokens = [];
    tokensSnapshot.forEach( function(doc) {
        const tokenId = doc.data().tokenId;
        tokens.push(tokenId)
      }
    )

    console.log("comment number:"+cv.data().comments);
    
    const message = {
      //By defalut,notification messages are collapsible
      data: {
        category:cv.data().category,
        title: 'Fidebox',
        body: `Your comment has ${comment.data().giggles} giggles and ${comment.data().child_comments+1} replies`,
        route: `${comment.data().route}`,
        type:'comment',
        comment_id:reply_comment_id,
        cv_id:cv.data().cvId,
        cv_giggles: `${cv.data().giggles}`,
        cv_comments:`${cv.data().comments+1}`,
        notifyee_id:commenter_id
      }
    };

    const response = await admin.messaging().sendToDevice(tokens, message);
    // For each message check if there was an error.
    const tokensToRemove = [];
    response.results.forEach((result, index) => {
      const error = result.error;
      if (error) {
        console.error('Failure sending notification to', tokens[index], error);
        // Cleanup the tokens who are not registered anymore.
        if (error.code === 'messaging/invalid-registration-token' ||
          error.code === 'messaging/registration-token-not-registered') {
          tokensToRemove.push(admin.firestore().collection('users').doc(commenter_id).collection('token')
            .doc(tokens[index]).delete());
        }
      }
    });
    await Promise.all(tokensToRemove);

    }


    try {
      await admin.firestore().runTransaction(async t =>{

     //check if the notification already exists   
     try {
      var commentReplyNotification= await t.get(admin.firestore().collection('users').doc(commenter_id).collection('comment_reply_notifications').doc(cv.data().cvId));
      } catch (error) {

     console.log(error);
     }

     if(!commentReplyNotification.exists){

      //update notifications  
  
      let commentReplyRef= admin.firestore().collection('users').doc(commenter_id).collection('comment_reply_notifications').doc(cv.data().cvId);  
      let commentReplyCountRef=admin.firestore().collection('users').doc(commenter_id).collection('--stat--').doc("notifications");
        //increase comment count of the cv   
      let commentReplyIncrementRef=admin.firestore().collection('users').doc(commenter_id).collection('comments').doc(comment_id);


      return Promise.all([t.set(commentReplyRef,notification),  
                          t.update(commentReplyCountRef,{comment_reply_notifications: admin.firestore.FieldValue.increment(1),comment_reply_notifications_eventId:eventId}),
                        t.update(commentReplyIncrementRef,{comments: admin.firestore.FieldValue.increment(1)})])
      } else{
       return admin.firestore().collection('users').doc(commenter_id).collection('comments').doc(comment_id).update({comments: admin.firestore.FieldValue.increment(1)});
      }

  })
  } catch(error){
    console.log('Error');
  }

  });

  //no Comment Reply Deletion (user can't delete comment)


   ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   exports.newUserCreateCollections = functions.firestore.document('users/{userId}').
   onCreate(async (snap, context)=> {

    const userId = context.params.userId;
    const eventId=context.eventId;

    //write the notification to the user collection
    const settings={
      change_on_cv:true,
      change_on_comment:true,
      userId:userId
    }

    //write the notification to the user collection
    const CVs={
      count:0,
      eventId:eventId
    }

    const comments={
      count:0,
      eventId:eventId
    }

    const notifications={
      comment_giggle_notifications:0,
      comment_reply_notifications:0,
      cv_giggle_notifications:0,
      cv_comment_notifications:0
    }

    const k=snap.data().username;
    const username={
      username:k,
      userId:userId
    }

    //incease stat in --stat--, user
    const incrementStatUser=admin.firestore().collection('--stat--').doc("users").update({count: admin.firestore.FieldValue.increment(1)});
    const addNewUserName=admin.firestore().collection("username").doc(k).set(username);
    const createSettingsPromise = admin.firestore().collection('users').doc(userId).collection('settings').doc("notificationSettings").set(settings);
    const createCVsStatPromise = admin.firestore().collection('users').doc(userId).collection('--stat--').doc("CVs").set(CVs);
    const createCommentsPromise= admin.firestore().collection('users').doc(userId).collection('--stat--').doc("comments").set(comments);
    const createNotificationsPromise= admin.firestore().collection('users').doc(userId).collection('--stat--').doc("notifications").set(notifications);
    
   await Promise.all([incrementStatUser,addNewUserName,createSettingsPromise,createCVsStatPromise,createCommentsPromise,createNotificationsPromise]);
    
  });


     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     exports.onAddUserCV = functions.firestore.document('users/{userId}/CVs/{cvId}').
     onCreate(async (snap, context)=> {
  
      const userId = context.params.userId;
      const eventId=context.eventId;
      //make sure no duplicate
      try{
        
      const previousCVStat= await admin.firestore().collection('users').doc(userId).collection('--stat--').doc("CVs").get();

      if(previousCVStat.data().eventId===eventId){
        return;
      }

      } catch(error){
        console.log(error);
      }

      try{
        await admin.firestore().collection('users').doc(userId).collection('--stat--').doc("CVs").update({count: admin.firestore.FieldValue.increment(1),eventId: eventId});
      } catch(error){
        console.log(error);
      }
      
    });

 
     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     exports.onAddUserComment = functions.firestore.document('users/{userId}/comments/{commentId}').
     onCreate(async (snap, context)=> {
  
      const userId = context.params.userId;
      const eventId=context.eventId;
      //make sure no duplicate
      try{
        
      const previousCommentStat= await admin.firestore().collection('users').doc(userId).collection('--stat--').doc("comments").get();

      if(previousCommentStat.data().eventId===eventId){
        return;
      }

      } catch(error){
        console.log(error);
      }

      try{
        await admin.firestore().collection('users').doc(userId).collection('--stat--').doc("comments").update({count: admin.firestore.FieldValue.increment(1),eventId: eventId});
      } catch(error){
        console.log(error);
      }
      
    });

    //// Football:start
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     exports.onAddFootballCV = functions.firestore.document('Football/{cvId}').
     onCreate( (snap, context)=> {

      const eventId=context.eventId;
     return onAddCV(eventId, "Football");
      
    });

    
    //// Life:start
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    exports.onAddLifeCV = functions.firestore.document('Life/{cvId}').
    onCreate( (snap, context)=> {

     const eventId=context.eventId;
     return onAddCV(eventId, "Life");

   });
   

   //// Politics:start
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    exports.onAddPolitcsCV = functions.firestore.document('Politics/{cvId}').
    onCreate( (snap, context)=> {

     const eventId=context.eventId;
     return onAddCV(eventId, "Politics");

   });

   //// Relationship:start
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    exports.onAddRelationshipCV = functions.firestore.document('Relationship/{cvId}').
    onCreate( (snap, context)=> {

     const eventId=context.eventId;
     return onAddCV(eventId, "Relationship");

   });


   async function onAddCV (eventId,category){
      //make sure no duplicate
      try{    
        
        const previousStat= await admin.firestore().collection('--stat--').doc(category).get();
    
        if(previousStat.data().eventId===eventId){
          return console.log("Error");
        } else{
          await admin.firestore().collection('--stat--').doc(category).update({count: admin.firestore.FieldValue.increment(1)});
        }
    
        } catch(error){
        return  console.log(error);
        }
      }



    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    exports.onAddFootballCVCommentOrGiggle0 = functions.firestore.document('Football/{cvId}/{commentsOrGiggles}/{commentOrGigglerId}').
    onCreate( (snap, context)=> {

     const cvId=context.params.cvId;
     const eventId=context.eventId;
     const type=context.params.commentsOrGiggles;

     const commentIdOrGigglerId = snap.data();

     return onAddCVCommentOrGiggle0(cvId,eventId,type,"Football",commentIdOrGigglerId);
   });


   ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   exports.onAddLifeCVCommentOrGiggle0 = functions.firestore.document('Life/{cvId}/{commentsOrGiggles}/{commentOrGigglerId}').
   onCreate( (snap, context)=> {

    const cvId=context.params.cvId;
    const eventId=context.eventId;
    const type=context.params.commentsOrGiggles;

    const commentIdOrGigglerId = snap.data();

    return onAddCVCommentOrGiggle0(cvId,eventId,type,"Life",commentIdOrGigglerId);
  });


  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  exports.onAddPoliticsCVCommentOrGiggle0 = functions.firestore.document('Politics/{cvId}/{commentsOrGiggles}/{commentOrGigglerId}').
  onCreate( (snap, context)=> {

   const cvId=context.params.cvId;
   const eventId=context.eventId;
   const type=context.params.commentsOrGiggles;

   const commentIdOrGigglerId = snap.data();

   return onAddCVCommentOrGiggle0(cvId,eventId,type,"Politics",commentIdOrGigglerId);
 });



 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 exports.onAddRelationshipCVCommentOrGiggle0 = functions.firestore.document('Relationship/{cvId}/{commentsOrGiggles}/{commentOrGigglerId}').
 onCreate( (snap, context)=> {

  const cvId=context.params.cvId;
  const eventId=context.eventId;
  const type=context.params.commentsOrGiggles;

  const commentIdOrGigglerId = snap.data();

  return onAddCVCommentOrGiggle0(cvId,eventId,type,"Relationship",commentIdOrGigglerId);
});


   async function onAddCVCommentOrGiggle0(cvId,eventId,type,category,commentIdOrGigglerId){

    try{
      var cv= await admin.firestore().collection(category).doc(cvId).get();

      //first differentiate between comment/giggle, then ensure idempotency
      if(type==="giggles"){

       if(cv.data().giggle_add_event===eventId){
         return;
       }

      } else{
        
       if(cv.data().commentEventId===eventId){
         return;
       }
      }

      } catch(error){
        console.log(error);
      }

       var giggles=cv.data().giggles;
       var comments=cv.data().comments;

       console.log(cv.data().comments);
      console.log(comments);

        //write the notification to the user collection
    var notification={
      cvId:cv.data().cvId,
      commentId:commentIdOrGigglerId
     }

    try{
      if(type==="giggles"){
      giggles=giggles+1;

      await admin.firestore().collection(category).doc(cvId).update({giggles: admin.firestore.FieldValue.increment(1),giggle_add_eventId:eventId});

      } else{
        comments=comments+1;

       let batch = admin.firestore().batch();

       //increase users->CV->comments

       const userCVCommentRef=admin.firestore().collection("users").doc(cv.data().userId).collection("CVs").doc(cvId);

       const cvCommentRef=admin.firestore().collection(category).doc(cvId);


       batch.update(userCVCommentRef,{comments: admin.firestore.FieldValue.increment(1),commentEventId:eventId});
       batch.update(cvCommentRef,{comments: admin.firestore.FieldValue.increment(1), commentEventId:eventId});

  
       //check if the notification already exists   
     try {
      var cvCommentNotification= await admin.firestore().collection('users').doc(cv.data().userId).collection('cv_comment_notifications').doc(cvId).get();
      } catch (error) {

     console.log(error);
     }

       if (cv.data().userId !== commentIdOrGigglerId.commenterId && !cvCommentNotification.exists){

        const userCVCommentNotification= admin.firestore().collection('users').doc(cv.data().userId).collection('cv_comment_notifications').doc(cvId);

        const userNotificationRef=admin.firestore().collection("users").doc(cv.data().userId).collection("--stat--").doc("notifications");

       batch.set(userCVCommentNotification, notification);
       batch.update(userNotificationRef,{cv_comment_notifications: admin.firestore.FieldValue.increment(1), cv_comment_notifications_add_event:eventId});
       }

      await batch.commit();


      // if not self-comment, then send
      if (cv.data().userId !== commentIdOrGigglerId.commenterId) {

      console.log("poster not the same");
      // Get the list of device notification tokens.
      const tokensSnapshot =await admin.firestore().collection('users').doc(cv.data().userId).collection('token').get();

      await sendNotificationWithSelfIncrement(tokensSnapshot,cv,giggles,comments); 
      }

      }

    } catch(error){
      console.log(error);
    }
   }


   async function sendNotificationWithSelfIncrement(tokensSnapshot,cv,giggles,comments){
  
    // Check if there are any device tokens.
    if (tokensSnapshot.size === 0) {
      return console.log('There are no notification tokens to send to.');
    }
    console.log('There are', tokensSnapshot.size, 'tokens to send notifications to.');
    
    const tokens = [];
    tokensSnapshot.forEach( function(doc) {
        const tokenId = doc.data().tokenId;
        tokens.push(tokenId)
      }
    )
    const message = {
      //By defalut,notification messages are collapsible
      data: {
        category:cv.data().category,
        title: 'Fidebox',
        body: `Your CV has ${giggles} giggles and ${comments} comments`,
        cv_id:cv.data().cvId,
        type:'CV',
        cv_giggles: `${giggles}`,
        cv_comments:`${comments}`,
        notifyee_id:`${cv.data().userId}`
      }
    };
 
    const response = await admin.messaging().sendToDevice(tokens, message);
    // For each message check if there was an error.
    const tokensToRemove = [];
    response.results.forEach((result, index) => {
      const error = result.error;
      if (error) {
        console.error('Failure sending notification to', tokens[index], error);
        // Cleanup the tokens who are not registered anymore.
        if (error.code === 'messaging/invalid-registration-token' ||
          error.code === 'messaging/registration-token-not-registered') {
          tokensToRemove.push(admin.firestore().collection('users').doc(cv.data().userId).collection('token')
            .doc(tokens[index]).delete());
        }
      }
    });
    await Promise.all(tokensToRemove);
 }



    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    exports.onDeleteFootballCVGiggle0 = functions.firestore.document('Football/{cvId}/giggles/{gigglerId}').
    onDelete((snap, context)=> {

     const cvId=context.params.cvId;
     const eventId=context.eventId;
     
    return onDeleteCVGiggle0(cvId,eventId,"Football");
   });

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    exports.onDeleteLifeCVGiggle0 = functions.firestore.document('Life/{cvId}/giggles/{gigglerId}').
    onDelete((snap, context)=> {

     const cvId=context.params.cvId;
     const eventId=context.eventId;
     
    return onDeleteCVGiggle0(cvId,eventId,"Life");
   });

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    exports.onDeletePoliticsCVGiggle0 = functions.firestore.document('Politics/{cvId}/giggles/{gigglerId}').
    onDelete((snap, context)=> {

     const cvId=context.params.cvId;
     const eventId=context.eventId;
     
   return  onDeleteCVGiggle0(cvId,eventId,"Politics");
   });


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    exports.onDeleteRelationshipCVGiggle0 = functions.firestore.document('Relationship/{cvId}/giggles/{gigglerId}').
    onDelete((snap, context)=> {

     const cvId=context.params.cvId;
     const eventId=context.eventId;
     
    return onDeleteCVGiggle0(cvId,eventId,"Relationship");
   });



   async function onDeleteCVGiggle0(cvId,eventId,category){
    try{
      var cv= (await admin.firestore().collection(category).doc(cvId).get()).data();

       if(cv.giggleDeleteEventId===eventId){
         return;
       }
    
      return admin.firestore().collection(category).doc(cvId).update({giggles: admin.firestore.FieldValue.increment(-1),giggleDeleteEventId:eventId});
   
    } catch(error){
      console.log(error);
    }
   }



   ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   exports.onAddFootballCVCommentOrGiggle1 = functions.firestore.document('Football/{cvId}/comments/{commentId}/{commentsOrGiggles}/{commentOrGigglerId}').
   onCreate(async (snap, context)=> {

    const cvId=context.params.cvId;
    const eventId=context.eventId;
    const type=context.params.commentsOrGiggles;
    const commentId=context.params.commentId;
    const commentIdOrGigglerId = snap.data();

    return onAddCVCommentOrGiggle1(cvId,eventId,type,commentId,"Football",commentIdOrGigglerId);
    
  });


  
   ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   exports.onAddLifeCVCommentOrGiggle1 = functions.firestore.document('Life/{cvId}/comments/{commentId}/{commentsOrGiggles}/{commentOrGigglerId}').
   onCreate(async (snap, context)=> {

    const cvId=context.params.cvId;
    const eventId=context.eventId;
    const type=context.params.commentsOrGiggles;
    const commentId=context.params.commentId;
    const commentIdOrGigglerId = snap.data();

    return onAddCVCommentOrGiggle1(cvId,eventId,type,commentId,"Life",commentIdOrGigglerId);
    
  });

  
   ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   exports.onAddPoliticsCVCommentOrGiggle1 = functions.firestore.document('Politics/{cvId}/comments/{commentId}/{commentsOrGiggles}/{commentOrGigglerId}').
   onCreate(async (snap, context)=> {

    const cvId=context.params.cvId;
    const eventId=context.eventId;
    const type=context.params.commentsOrGiggles;
    const commentId=context.params.commentId;
    const commentIdOrGigglerId = snap.data();

    return onAddCVCommentOrGiggle1(cvId,eventId,type,commentId,"Politics",commentIdOrGigglerId);
    
  });

  
   ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   exports.onAddRelationshipCVCommentOrGiggle1 = functions.firestore.document('Relationship/{cvId}/comments/{commentId}/{commentsOrGiggles}/{commentOrGigglerId}').
   onCreate(async (snap, context)=> {

    const cvId=context.params.cvId;
    const eventId=context.eventId;
    const type=context.params.commentsOrGiggles;
    const commentId=context.params.commentId;
    const commentIdOrGigglerId = snap.data();

    return onAddCVCommentOrGiggle1(cvId,eventId,type,commentId,"Relationship",commentIdOrGigglerId);
    
  });

 async function onAddCVCommentOrGiggle1(cvId,eventId,type,commentId,category,commentIdOrGigglerId){
    try{
      var comment= (await admin.firestore().collection(category).doc(cvId).collection("comments").doc(commentId).get()).data();
      var cv= await admin.firestore().collection(category).doc(cvId).get();
      
      //first differentiate between comment/giggle, then ensure idempotency
      if(type==="giggles"){

       if(comment.giggle_add_eventId===eventId){
         return;
       }

      } else{
        
       if(comment.commentEventId===eventId){
         return;
       }
      }

      } catch(error){
        console.log(error);
      }


      var giggles=cv.data().giggles;
      var comments=cv.data().comments;

    try{
      if(type==="giggles"){
       giggles=giggles+1; 
        
      return admin.firestore().collection(category).doc(cvId)
                          .collection("comments").doc(commentId).update({giggles: admin.firestore.FieldValue.increment(1),giggle_add_eventId:eventId});

      } else{
        comments=comments+1;

        var notification={
          cvId:cvId,
          commentId:commentId
        }
  
            // if not self-comment, then send
      if (cv.data().userId !== commentIdOrGigglerId.commenterId) {

        console.log("poster not the same");
        // Get the list of device notification tokens.
        const tokensSnapshot =await admin.firestore().collection('users').doc(comment.commenterId).collection('token').get();
  
        await sendNotificationWithSelfIncrement(tokensSnapshot,cv,giggles,comments); 
        }

       let batch = admin.firestore().batch();

       //increase users->CV->comments
       const userCVCommentRef=admin.firestore().collection("users").doc(cv.data().userId).collection("CVs").doc(cvId);

       const cvComment0Ref=admin.firestore().collection(category).doc(cvId);

       const cvComment1Ref=admin.firestore().collection(category).doc(cvId).collection("comments").doc(commentId);

       //will be incremented in userCommentReplyNotifications
       //const commentLogRef=admin.firestore().collection("users").doc(comment.commenterId).collection("comments").doc(comment.commentId);
       
       batch.update(userCVCommentRef,{comments: admin.firestore.FieldValue.increment(1),commentEventId:eventId});
       batch.update(cvComment0Ref,{comments: admin.firestore.FieldValue.increment(1),commentEventId:eventId});
       batch.update(cvComment1Ref,{child_comments: admin.firestore.FieldValue.increment(1), commentEventId:eventId});
      // batch.update(commentLogRef,{child_comments: admin.firestore.FieldValue.increment(1), commentEventId:eventId});

    //check if the notification already exists   
     try {
      var cvCommentNotification= await admin.firestore().collection('users').doc(cv.data().userId).collection('cv_comment_notifications').doc(cvId).get();
      } catch (error) {

     console.log(error);
     }

      if (cv.data().userId !== commentIdOrGigglerId.commenterId && !cvCommentNotification.exists){

        const userCVCommentNotification= admin.firestore().collection('users').doc(cv.data().userId).collection('cv_comment_notifications').doc(cvId);

        const userNotificationRef=admin.firestore().collection("users").doc(cv.data().userId).collection("--stat--").doc("notifications");

       batch.set(userCVCommentNotification, notification);
       batch.update(userNotificationRef,{cv_comment_notifications: admin.firestore.FieldValue.increment(1), cv_comment_notifications_add_event:eventId});
       }

       if (comment.commenterId !== commentIdOrGigglerId.commenterId){

        const userCVComment= admin.firestore().collection('users').doc(comment.commenterId).collection('comments').doc(comment.commentId).collection("comments").doc(commentIdOrGigglerId.commentId);

       batch.set(userCVComment, commentIdOrGigglerId);
       } 

      await batch.commit();

      }

    } catch(error){
      console.log(error);
    }
  }


 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 exports.onDeleteFootballCVGiggle1 = functions.firestore.document('Football/{cvId}/comments/{commentId}/giggles/{gigglerId}').
 onDelete(async (snap, context)=> {

  const cvId=context.params.cvId;
  const eventId=context.eventId;
  const commentId=context.params.commentId;

 return onDeleteCVGiggle1(cvId,eventId,commentId,"Football");

});


 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 exports.onDeleteLifeCVGiggle1 = functions.firestore.document('Life/{cvId}/comments/{commentId}/giggles/{gigglerId}').
 onDelete(async (snap, context)=> {

  const cvId=context.params.cvId;
  const eventId=context.eventId;
  const commentId=context.params.commentId;

 return onDeleteCVGiggle1(cvId,eventId,commentId,"Life");

});


 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 exports.onDeletePoliticsCVGiggle1 = functions.firestore.document('Politics/{cvId}/comments/{commentId}/giggles/{gigglerId}').
 onDelete(async (snap, context)=> {

  const cvId=context.params.cvId;
  const eventId=context.eventId;
  const commentId=context.params.commentId;

  return onDeleteCVGiggle1(cvId,eventId,commentId,"Politics");

});


 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 exports.onDeleteRelationshipCVGiggle1 = functions.firestore.document('Relationship/{cvId}/comments/{commentId}/giggles/{gigglerId}').
 onDelete(async (snap, context)=> {

  const cvId=context.params.cvId;
  const eventId=context.eventId;
  const commentId=context.params.commentId;

  return onDeleteCVGiggle1(cvId,eventId,commentId,"Relationship");

});

async function onDeleteCVGiggle1(cvId,eventId,commentId,category){
  try{
    var comment= (await admin.firestore().collection(category).doc(cvId).collection("comments").doc(commentId).get()).data();

     if(comment.giggleDeleteEventId===eventId){
       return;
     }
  
    return admin.firestore().collection(category).doc(cvId).collection("comments").doc(commentId).update({giggles: admin.firestore.FieldValue.increment(-1),giggleDeleteEventId:eventId});
 
  } catch(error){
    console.log(error);
  }
}


  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  exports.onAddFootballCVCommentOrGiggle2 = functions.firestore.document('Football/{cvId}/comments/{commentId}/comments/{commentId2}/{commentsOrGiggles}/{commentOrGigglerId}').
  onCreate(async (snap, context)=> {

   const cvId=context.params.cvId;
   const eventId=context.eventId;
   const type=context.params.commentsOrGiggles;
   const commentId=context.params.commentId;
   const commentId2=context.params.commentId2;
   const commentIdOrGigglerId = snap.data();
   
   return onAddCVCommentOrGiggle2(cvId,eventId,type,commentId,commentId2,"Football",commentIdOrGigglerId);
 });

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  exports.onAddLifeCVCommentOrGiggle2 = functions.firestore.document('Life/{cvId}/comments/{commentId}/comments/{commentId2}/{commentsOrGiggles}/{commentOrGigglerId}').
  onCreate(async (snap, context)=> {

   const cvId=context.params.cvId;
   const eventId=context.eventId;
   const type=context.params.commentsOrGiggles;
   const commentId=context.params.commentId;
   const commentId2=context.params.commentId2;
   const commentIdOrGigglerId = snap.data();
   
   return onAddCVCommentOrGiggle2(cvId,eventId,type,commentId,commentId2,"Life",commentIdOrGigglerId);
 });


   ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   exports.onAddRelationshipCVCommentOrGiggle2 = functions.firestore.document('Relationship/{cvId}/comments/{commentId}/comments/{commentId2}/{commentsOrGiggles}/{commentOrGigglerId}').
   onCreate(async (snap, context)=> {
 
    const cvId=context.params.cvId;
    const eventId=context.eventId;
    const type=context.params.commentsOrGiggles;
    const commentId=context.params.commentId;
    const commentId2=context.params.commentId2;
    const commentIdOrGigglerId = snap.data();
    
    return onAddCVCommentOrGiggle2(cvId,eventId,type,commentId,commentId2,"Relationship",commentIdOrGigglerId);
  });


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    exports.onAddPoliticsCVCommentOrGiggle2 = functions.firestore.document('Politics/{cvId}/comments/{commentId}/comments/{commentId2}/{commentsOrGiggles}/{commentOrGigglerId}').
    onCreate(async (snap, context)=> {
  
     const cvId=context.params.cvId;
     const eventId=context.eventId;
     const type=context.params.commentsOrGiggles;
     const commentId=context.params.commentId;
     const commentId2=context.params.commentId2;
     const commentIdOrGigglerId = snap.data();
     
     return onAddCVCommentOrGiggle2(cvId,eventId,type,commentId,commentId2,"Politics",commentIdOrGigglerId);
   });


 async function onAddCVCommentOrGiggle2(cvId,eventId,type,commentId,commentId2,category,commentIdOrGigglerId){
   
  try{
    var comment= (await admin.firestore().collection(category).doc(cvId)
                                    .collection("comments").doc(commentId).collection("comments").doc(commentId2).get()).data();
    var cv= await admin.firestore().collection(category).doc(cvId).get();
     
     //first differentiate between comment/giggle, then ensure idempotency
     if(type==="giggles"){

      if(comment.giggle_add_eventId===eventId){
        return;
      }

     } else{
       
      if(comment.commentEventId===eventId){
        return;
      }
     }

     } catch(error){
       console.log(error);
     }

     var giggles=cv.data().giggles;
     var comments=cv.data().comments;

   try{
     if(type==="giggles"){
      giggles=giggles+1; 

     return admin.firestore().collection(category).doc(cvId).collection("comments").doc(commentId).collection("comments").doc(commentId2)
                              .update({giggles: admin.firestore.FieldValue.increment(1),giggle_add_eventId:eventId});

     } else{

      var notification={
        cvId:cvId,
        commentId:commentId
      }

      comments=comments+1; 

      // if not self-giggle, then send
      if (cv.data().userId !== commentIdOrGigglerId.commenterId) {

        console.log("poster not the same");
        // Get the list of device notification tokens.
        const tokensSnapshot =await admin.firestore().collection('users').doc(comment.commenterId).collection('token').get();
  
        await sendNotificationWithSelfIncrement(tokensSnapshot,cv,giggles,comments); 
        }

      let batch = admin.firestore().batch();

      //increase users->CV->comments
      const userCVCommentRef=admin.firestore().collection("users").doc(cv.data().userId).collection("CVs").doc(cvId);

      const cvComment0Ref=admin.firestore().collection(category).doc(cvId);

      const cvComment2Ref=admin.firestore().collection(category).doc(cvId).collection("comments").doc(commentId).collection("comments").doc(commentId2);
//will be incremented in userCommentReplyNotifications
   //   const commentLogRef=admin.firestore().collection("users").doc(comment.commenterId).collection("comments").doc(comment.commentId);

      batch.update(userCVCommentRef,{comments: admin.firestore.FieldValue.increment(1),commentEventId:eventId});
      batch.update(cvComment0Ref,{comments: admin.firestore.FieldValue.increment(1),commentEventId:eventId})
      batch.update(cvComment2Ref,{child_comments: admin.firestore.FieldValue.increment(1), commentEventId:eventId});
  //    batch.update(commentLogRef,{child_comments: admin.firestore.FieldValue.increment(1), commentEventId:eventId});

      //check if the notification already exists   
      try {
        var cvCommentNotification= await admin.firestore().collection('users').doc(cv.data().userId).collection('cv_comment_notifications').doc(cvId).get();
        } catch (error) {
  
       console.log(error);
       }  

       if (cv.data().userId !== commentIdOrGigglerId.commenterId && !cvCommentNotification.exists){

        const userCVCommentNotification= admin.firestore().collection('users').doc(cv.data().userId).collection('cv_comment_notifications').doc(cvId);

        const userNotificationRef=admin.firestore().collection("users").doc(cv.data().userId).collection("--stat--").doc("notifications");

       batch.set(userCVCommentNotification, notification);
       batch.update(userNotificationRef,{cv_comment_notifications: admin.firestore.FieldValue.increment(1), cv_comment_notifications_add_event:eventId});
       }


       if (comment.commenterId !== commentIdOrGigglerId.commenterId){

        const userCVComment= admin.firestore().collection('users').doc(comment.commenterId).collection('comments').doc(comment.commentId).collection("comments").doc(commentIdOrGigglerId.commentId);

       batch.set(userCVComment, commentIdOrGigglerId);
       } 

      await batch.commit();

     }

   } catch(error){
     console.log(error);
   }
 }



 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 exports.onDeleteFootballCVGiggle2 = functions.firestore.document('Football/{cvId}/comments/{commentId}/comments/{commentId2}/giggles/{gigglerId}').
 onDelete(async (snap, context)=> {

  const cvId=context.params.cvId;
  const eventId=context.eventId;
  const commentId=context.params.commentId;
  const commentId2=context.params.commentId2;

  return onDeleteCVGiggle2(cvId,eventId,commentId,commentId2,"Football");

});


 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 exports.onDeleteLifeCVGiggle2 = functions.firestore.document('Life/{cvId}/comments/{commentId}/comments/{commentId2}/giggles/{gigglerId}').
 onDelete(async (snap, context)=> {

  const cvId=context.params.cvId;
  const eventId=context.eventId;
  const commentId=context.params.commentId;
  const commentId2=context.params.commentId2;

  return onDeleteCVGiggle2(cvId,eventId,commentId,commentId2,"Life");

});

 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 exports.onDeletePoliticsCVGiggle2 = functions.firestore.document('Politics/{cvId}/comments/{commentId}/comments/{commentId2}/giggles/{gigglerId}').
 onDelete(async (snap, context)=> {

  const cvId=context.params.cvId;
  const eventId=context.eventId;
  const commentId=context.params.commentId;
  const commentId2=context.params.commentId2;

  return onDeleteCVGiggle2(cvId,eventId,commentId,commentId2,"Politics");

});

 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 exports.onDeleteRelationshipCVGiggle2 = functions.firestore.document('Relationship/{cvId}/comments/{commentId}/comments/{commentId2}/giggles/{gigglerId}').
 onDelete(async (snap, context)=> {

  const cvId=context.params.cvId;
  const eventId=context.eventId;
  const commentId=context.params.commentId;
  const commentId2=context.params.commentId2;

  return onDeleteCVGiggle2(cvId,eventId,commentId,commentId2,"Relationship");

});


async function onDeleteCVGiggle2(cvId,eventId,commentId,commentId2,category){

  try{
    var comment= (await admin.firestore().collection(category).doc(cvId)
                                    .collection("comments").doc(commentId)
                                    .collection("comments").doc(commentId2).get()).data();

     if(comment.giggleDeleteEventId===eventId){
       return;
     }
  
    return admin.firestore().collection(category).doc(cvId)
    .collection("comments").doc(commmentId).collection("comments").doc(commentId2)
    .update({giggles: admin.firestore.FieldValue.increment(-1),giggleDeleteEventId:eventId});
 
  } catch(error){
    console.log(error);
  }
}

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  exports.onAddFootballCVGiggle3 = functions.firestore.document('Football/{cvId}/comments/{commentId}/comments/{commentId2}/comments/{commentId3}/giggles/{gigglerId}').
  onCreate(async (snap, context)=> {

   const cvId=context.params.cvId;
   const eventId=context.eventId;
   const commentId=context.params.commentId;
   const commentId2=context.params.commentId2;
   const commentId3=context.params.commentId3;
   
   return onAddCVCommentOrGiggle3(cvId,eventId,commentId,commentId2,commentId3,"Football");
 }); 

   ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   exports.onAddPoliticsCVGiggle3 = functions.firestore.document('Politics/{cvId}/comments/{commentId}/comments/{commentId2}/comments/{commentId3}/giggles/{gigglerId}').
   onCreate(async (snap, context)=> {
 
    const cvId=context.params.cvId;
    const eventId=context.eventId;
    const commentId=context.params.commentId;
    const commentId2=context.params.commentId2;
    const commentId3=context.params.commentId3;
    
    return onAddCVCommentOrGiggle3(cvId,eventId,commentId,commentId2,commentId3,"Politics");
  });

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    exports.onAddLifeCVGiggle3 = functions.firestore.document('Life/{cvId}/comments/{commentId}/comments/{commentId2}/comments/{commentId3}/giggles/{gigglerId}').
    onCreate(async (snap, context)=> {
  
     const cvId=context.params.cvId;
     const eventId=context.eventId;
     const commentId=context.params.commentId;
     const commentId2=context.params.commentId2;
     const commentId3=context.params.commentId3;
     
     return onAddCVCommentOrGiggle3(cvId,eventId,commentId,commentId2,commentId3,"Life");
   });

     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  exports.onAddRelationshipCVGiggle3 = functions.firestore.document('Relationship/{cvId}/comments/{commentId}/comments/{commentId2}/comments/{commentId3}/giggles/{gigglerId}').
  onCreate(async (snap, context)=> {

   const cvId=context.params.cvId;
   const eventId=context.eventId;
   const commentId=context.params.commentId;
   const commentId2=context.params.commentId2;
   const commentId3=context.params.commentId3;
   
   return onAddCVCommentOrGiggle3(cvId,eventId,commentId,commentId2,commentId3,"Relationship");
 });

async function onAddCVCommentOrGiggle3(cvId,eventId,commentId,commentId2,commentId3,category){
  
   var comment= (await admin.firestore().collection(category).doc(cvId)
                                    .collection("comments").doc(commentId).collection("comments").doc(commentId2).collection("comments").doc(commentId3).get()).data();

   //first differentiate between comment/giggle, then ensure idempotency

    if(comment.giggle_add_eventId===eventId){
      return;
    }

  //only giggles are possible, notification will be done in userCommentGiggleNotification
   try{
     return admin.firestore().collection(category).doc(cvId).collection("comments").doc(commentId).collection("comments").doc(commentId2).collection("comments").doc(commentId3)
                              .update({giggles: admin.firestore.FieldValue.increment(1),giggle_add_eventId:eventId});
   } catch(error){
     console.log(error);
   }
}

 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 exports.onDeleteFootballCVCGiggle3 = functions.firestore.document('Football/{cvId}/comments/{commentId}/comments/{commentId2}/comments/{commentId3}/giggles/{gigglerId}').
 onDelete(async (snap, context)=> {

  const cvId=context.params.cvId;
  const eventId=context.eventId;
  const commentId=context.params.commentId;
  const commentId2=context.params.commentId2;
  const commentId3=context.params.commentId3;
  
  return onDeleteCVCommentOrGiggle3(cvId,eventId,commentId,commentId2,commentId3,"Football");
}); 

 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 exports.onDeletePolitcsCVGiggle3 = functions.firestore.document('Politics/{cvId}/comments/{commentId}/comments/{commentId2}/comments/{commentId3}/giggles/{gigglerId}').
 onDelete(async (snap, context)=> {

  const cvId=context.params.cvId;
  const eventId=context.eventId;
  const commentId=context.params.commentId;
  const commentId2=context.params.commentId2;
  const commentId3=context.params.commentId3;
  
  return onDeleteCVCommentOrGiggle3(cvId,eventId,commentId,commentId2,commentId3,"Politics");
}); 

 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 exports.onDeleteLifeCVGiggle3 = functions.firestore.document('Life/{cvId}/comments/{commentId}/comments/{commentId2}/comments/{commentId3}/giggles/{gigglerId}').
 onDelete(async (snap, context)=> {

  const cvId=context.params.cvId;
  const eventId=context.eventId;
  const commentId=context.params.commentId;
  const commentId2=context.params.commentId2;
  const commentId3=context.params.commentId3;
  
  return onDeleteCVCommentOrGiggle3(cvId,eventId,commentId,commentId2,commentId3,"Life");
}); 

 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 exports.onDeleteRelationshipCVGiggle3 = functions.firestore.document('Relationship/{cvId}/comments/{commentId}/comments/{commentId2}/comments/{commentId3}/giggles/{gigglerId}').
 onDelete(async (snap, context)=> {

  const cvId=context.params.cvId;
  const eventId=context.eventId;
  const commentId=context.params.commentId;
  const commentId2=context.params.commentId2;
  const commentId3=context.params.commentId3;
  
  return onDeleteCVCommentOrGiggle3(cvId,eventId,commentId,commentId2,commentId3,"Relationship");
}); 


async function onDeleteCVCommentOrGiggle3(cvId,eventId,commentId,commentId2,commentId3,category){
   
  try{
    return admin.firestore().collection(category).doc(cvId).collection("comments").doc(commentId).collection("comments").doc(commentId2).collection("comments").doc(commentId3)
                             .update({giggles: admin.firestore.FieldValue.increment(-1),giggleDeleteEventId:eventId});
  } catch(error){
    console.log(error);
  }
}


exports.onDeleteCVGiggleNotification = functions.firestore.document('users/{userId}/cv_giggle_notifications/{cvId}').
 onDelete(async (snap, context)=> {

  const cvId=context.params.cvId;
  const eventId=context.eventId;
  const userId=context.params.userId;

  try{
    var notification= (await admin.firestore().collection("users").doc(userId)
                                    .collection("--stat--").doc("notifications").get()).data();

     if(notification.cv_giggle_notifications_delete_event===eventId){
       return;
     }
  
     return decrementNotification(userId,"cv_giggle_notifications","cv_giggle_notifications_delete_event",eventId);
 
  } catch(error){
    console.log(error);
  }
  
}); 



exports.onDeleteCVCommentNotification = functions.firestore.document('users/{userId}/cv_comment_notifications/{cvId}').
 onDelete(async (snap, context)=> {

  const eventId=context.eventId;
  const userId=context.params.userId;

  try{
    var notification= (await admin.firestore().collection("users").doc(userId)
                                    .collection("--stat--").doc("notifications").get()).data();

     if(notification.cv_comment_notifications_delete_event===eventId){
       return;
     }
  
     return decrementNotification(userId,"cv_comment_notifications","cv_comment_notifications_delete_event",eventId);
 
  } catch(error){
    console.log(error);
  }
  
}); 



exports.onDeleteCommentGiggleNotification = functions.firestore.document('users/{userId}/comment_giggle_notifications/{cvId}').
 onDelete(async (snap, context)=> {

  const eventId=context.eventId;
  const userId=context.params.userId;

  try{
    var notification= (await admin.firestore().collection("users").doc(userId)
                                    .collection("--stat--").doc("notifications").get()).data();

     if(notification.comment_giggle_notifications_delete_event===eventId){
       return;
     }
  
     return decrementNotification(userId,"comment_giggle_notifications","comment_giggle_notifications_delete_event",eventId);
 
  } catch(error){
    console.log(error);
  }
  
}); 


exports.onDeleteCommentReplyNotification = functions.firestore.document('users/{userId}/comment_reply_notifications/{cvId}').
 onDelete(async (snap, context)=> {

  const eventId=context.eventId;
  const userId=context.params.userId;

  try{
    var notification= (await admin.firestore().collection("users").doc(userId)
                                    .collection("--stat--").doc("notifications").get()).data();

     if(notification.comment_reply_notifications_delete_event===eventId){
       return;
     }
  
     return decrementNotification(userId,"comment_reply_notifications","comment_reply_notifications_delete_event",eventId);
 
  } catch(error){
    console.log(error);
  }
  
}); 



function decrementNotification(userId,notification,event,eventId){
  console.log("testing");

  return admin.firestore().collection("users").doc(userId).collection("--stat--").doc("notifications")
   .update({[notification]: admin.firestore.FieldValue.increment(-1),[event]:eventId});

}











   

