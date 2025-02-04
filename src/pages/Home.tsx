import { IonContent, IonPage, IonButton, IonInput } from '@ionic/react';
import './Home.css';
import { Metrics } from '@innerworks-me/iw-mobile-auth';
import { useState } from 'react';

const Home: React.FC = () => {
  const [inputValue, setInputValue] = useState("");


  return (
    <IonPage>
      <IonContent className="ion-content-container" fullscreen>
        <div className="button-container">
          <IonInput
            type="text"
            placeholder="Enter text"
            className="custom-input"
            onIonChange={(e) => setInputValue(e.detail.value!)}
            clearInput
          />
          <IonButton
            onClick={() => {
              try {
                Metrics.sendCollectedData({ 
                  projectId: 'project-id', 
                  socialId: inputValue, 
                });
              } catch (error) {
                console.error('Button click error:', error)
              }
            }}
          >Click</IonButton>
        </div>
      </IonContent>
    </IonPage>
  );
};

export default Home;
