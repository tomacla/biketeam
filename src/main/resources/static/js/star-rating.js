/**
 * Interactive Star Rating Component
 * Provides star-based rating functionality with mouse hover and click interactions
 */
class StarRating {
    constructor(element) {
        this.container = element;

        this.readonly = element.dataset.canRate ? element.dataset.canRate === "false" : true;
        this.averageRating = element.dataset.averageRating ? parseFloat(element.dataset.averageRating) : null;
        this.userRating = element.dataset.userRating ? parseFloat(element.dataset.userRating) : null;
        if (this.userRating === -1) {
            this.userRating = null;
        }
        this.ratingCount = element.dataset.ratingCount ? parseInt(element.dataset.ratingCount) : 0;
        this.mapId = element.dataset.mapId;
        this.teamId = element.dataset.teamId;

        this.hoveredRating = 0;

        if (this.readonly) {
            this.container.classList.add('readonly');
        } else {
            this.container.classList.add('interactive');
        }
        this.container.innerHTML = '';

        for (let i = 1; i <= 5; i++) {
            const star = document.createElement('span');
            star.className = 'star';
            star.dataset.rating = i;
            star.innerHTML = '★';
            this.container.appendChild(star);
        }

        this.info = document.createElement('span');
        this.info.className = 'rating-info';
        this.container.appendChild(this.info);

        if (!this.readonly) {
            this.bindEvents();
        }

        this.updateStarDisplay();
    }

    bindEvents() {
        const stars = this.container.querySelectorAll('.star');

        stars.forEach(star => {
            star.addEventListener('mouseenter', (e) => {
                this.hoveredRating = parseInt(e.target.dataset.rating);
                this.updateStarDisplay();
            });

            star.addEventListener('click', (e) => {
                const rating = parseInt(e.target.dataset.rating);
                this.setRating(rating);
            });
        });

        this.container.addEventListener('mouseleave', () => {
            this.hoveredRating = 0;
            this.updateStarDisplay();
        });
    }

    updateStarDisplay() {
        const stars = this.container.querySelectorAll('.star');
        var displayRating;
        if (this.hoveredRating > 0) {
            displayRating = this.hoveredRating
        } else if (this.averageRating) {
            displayRating = this.averageRating;
        } else {
            displayRating = 0;
        }

        stars.forEach((star, index) => {
            const starRating = index + 1;
            star.classList.remove('filled', 'empty', 'hovered');

            if (!this.readonly && this.hoveredRating > 0 && starRating <= this.hoveredRating) {
                star.classList.add('hovered');
            } else if (starRating <= displayRating) {
                star.classList.add('filled');
            } else {
                star.classList.add('empty');
            }
        });

        if (this.averageRating && this.ratingCount && this.ratingCount > 0) {
            const avg = parseFloat(this.averageRating).toFixed(1);
            this.info.textContent = `${avg}/5 (${this.ratingCount} ${this.ratingCount === 1 ? 'vote' : 'votes'})`;
            if (!this.readonly && this.userRating) {
                this.info.textContent += ` - vous : ${this.userRating}/5`;
            }
        } else {
            this.info.textContent = 'Pas de vote';
        }
    }

    setRating(rating) {
        if (this.readonly) return;

        const url = `/${this.teamId}/maps/${this.mapId}/rate`;

        // Show loading state
        this.container.classList.add('loading');

        fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `rating=${rating}`
        })
            .then(response => {
            if (!response.ok) {
                throw new Error('Failed to submit rating');
            }
            return response.json();
        })
            .then(data => {
            // Update the rating display with new data
            this.averageRating = data.averageRating;
            this.ratingCount = data.ratingCount;
            if (data.userRating) {
                this.userRating = data.userRating;
            } else {
                this.userRating = null;
            }
        })
            .catch(error => {
            console.error('Error submitting rating:', error);
        })
            .finally(() => {
            this.hoveredRating = 0;
            this.updateStarDisplay();
            this.container.classList.remove('loading');
        });
    }

}

// Auto-initialize all star ratings on page load
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.star-rating').forEach(element => new StarRating(element));
});
