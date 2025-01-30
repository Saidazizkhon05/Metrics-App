import { IonContent, IonPage, IonButton, IonInput } from '@ionic/react';
import './Home.css';
import {Metrics} from '@innerworks-me/iw-mobile-auth';

const Home: React.FC = () => {
  

  return (
    <IonPage>
      <IonContent className="ion-content-container" fullscreen>
        <div className="button-container">
          <IonInput
            type="text"
            placeholder="Enter text"
            className="custom-input"
            clearInput
          />
          <IonButton
            onClick={() => {
              try {
                Metrics.sendCollectedData("Button Clicked");
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
